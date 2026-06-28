package com.company.task.service;

import cn.hutool.core.util.XmlUtil;
import cn.hutool.http.HttpUtil;
import com.company.task.dto.NewsItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 资讯服务：调用第三方 API（Hacker News Firebase API）+ RSS（hnrss.org），
 * 获取科技/编程行业最新资讯，并支持按任务标签关联筛选。
 *
 * <p>AI 辅助说明：本模块对接公开第三方数据源，对接逻辑由 AI 协助实现。</p>
 *
 * <p>实现的要求：</p>
 * <ol>
 *   <li>调用公开第三方 API（Hacker News Firebase）+ RSS（hnrss.org）获取最新资讯；</li>
 *   <li>任务详情可按 tags 关联显示相关资讯（如创建 Java/Spring Boot 任务时获取相关新闻）；</li>
 *   <li>支持资讯展示、简单搜索、刷新（带 5 分钟内存缓存避免频控）。</li>
 * </ol>
 */
@Slf4j
@Service
public class NewsService {

    /** Hacker News Firebase API：Top Stories */
    private static final String HN_TOP_URL = "https://hacker-news.firebaseio.com/v0/topstories.json";
    /** Hacker News 单条详情 API */
    private static final String HN_ITEM_URL = "https://hacker-news.firebaseio.com/v0/item/%s.json";
    /** RSS 源（Hacker News 前页 RSS） */
    private static final String RSS_URL = "https://hnrss.org/frontpage";

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${app.news.default-size:15}")
    private int defaultSize;

    @Value("${app.news.enabled:true}")
    private boolean enabled;

    /** 缓存：key=源标识，value=资讯列表；TTL 5 分钟 */
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = TimeUnit.MINUTES.toMillis(5);

    /**
     * 获取最新资讯（合并 API + RSS 两个来源，去重）。
     *
     * @param keyword 搜索关键字（可空：空则返回全部最新）
     * @param size    返回数量上限
     */
    public List<NewsItem> getLatestNews(String keyword, Integer size) {
        int limit = (size == null || size <= 0) ? defaultSize : size;
        if (!enabled) {
            log.warn("资讯模块已禁用（app.news.enabled=false），返回空列表");
            return Collections.emptyList();
        }

        List<NewsItem> all = fetchAll();
        List<NewsItem> filtered = (keyword == null || keyword.isBlank())
                ? all
                : filterByKeyword(all, keyword);

        if (filtered.size() > limit) {
            filtered = filtered.subList(0, limit);
        }
        return filtered;
    }

    /**
     * 按任务标签关联获取资讯（任务详情/表单使用）。
     * 将 tags（逗号分隔）拆分为多个关键词，命中任一即返回。
     *
     * @param tags 任务标签，如 "Java,Spring Boot"
     * @param size 返回数量
     */
    public List<NewsItem> getNewsByTags(String tags, Integer size) {
        if (tags == null || tags.isBlank()) {
            return getLatestNews(null, size);
        }
        int limit = (size == null || size <= 0) ? 5 : size;
        String[] tagArr = Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        if (tagArr.length == 0) {
            return getLatestNews(null, size);
        }

        List<NewsItem> all = fetchAll();
        List<NewsItem> matched = new ArrayList<>();
        for (NewsItem item : all) {
            String matchedTag = matchAnyTag(item, tagArr);
            if (matchedTag != null) {
                item.setMatchedTag(matchedTag);
                matched.add(item);
                if (matched.size() >= limit) break;
            }
        }
        return matched;
    }

    // ===== 数据抓取 =====

    /**
     * 合并 API + RSS 两个来源，按标题去重。
     */
    private List<NewsItem> fetchAll() {
        List<NewsItem> result = new ArrayList<>();
        Map<String, NewsItem> dedup = new LinkedHashMap<>();

        // 1. RSS 源（速度较快，整页一次请求）
        for (NewsItem item : safeFetchRss()) {
            if (item.getTitle() != null && dedup.putIfAbsent(item.getTitle().toLowerCase(), item) == null) {
                result.add(item);
            }
        }

        // 2. Hacker News API 源
        for (NewsItem item : safeFetchHnApi()) {
            if (item.getTitle() != null && dedup.putIfAbsent(item.getTitle().toLowerCase(), item) == null) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * RSS 抓取（hnrss.org/frontpage），使用 Hutool HttpUtil + XmlUtil 解析。
     */
    private List<NewsItem> safeFetchRss() {
        try {
            List<NewsItem> cached = getCache("rss");
            if (cached != null) return cached;

            String xml = HttpUtil.get(RSS_URL, 8000);
            List<NewsItem> items = parseRssXml(xml);
            putCache("rss", items);
            return items;
        } catch (Exception e) {
            log.warn("RSS 抓取失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Hacker News API 抓取：先取 Top Stories ID 列表，再并发拉取每条详情。
     */
    private List<NewsItem> safeFetchHnApi() {
        try {
            List<NewsItem> cached = getCache("hn");
            if (cached != null) return cached;

            String idsJson = HttpUtil.get(HN_TOP_URL, 8000);
            JsonNode idArray = mapper.readTree(idsJson);
            int fetchCount = Math.min(20, idArray.size());

            List<NewsItem> items = new ArrayList<>();
            for (int i = 0; i < fetchCount; i++) {
                long id = idArray.get(i).asLong();
                NewsItem item = fetchHnItem(id);
                if (item != null) {
                    items.add(item);
                }
            }
            putCache("hn", items);
            return items;
        } catch (Exception e) {
            log.warn("Hacker News API 抓取失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private NewsItem fetchHnItem(long id) {
        try {
            String json = HttpUtil.get(String.format(HN_ITEM_URL, id), 6000);
            JsonNode node = mapper.readTree(json);
            if (node == null || node.path("title").isMissingNode()) {
                return null;
            }
            NewsItem item = new NewsItem();
            item.setTitle(node.path("title").asText());
            item.setUrl(node.has("url") ? node.get("url").asText() : "https://news.ycombinator.com/item?id=" + id);
            item.setSource("Hacker News");
            item.setSummary(node.path("title").asText());
            long time = node.path("time").asLong(0);
            item.setPublishedAt(time > 0 ? DATE_FMT.format(Instant.ofEpochSecond(time)) : "");
            return item;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析 RSS XML（标准 RSS 2.0 的 item 节点）。
     */
    private List<NewsItem> parseRssXml(String xml) {
        if (xml == null || xml.isBlank()) {
            return Collections.emptyList();
        }
        List<NewsItem> items = new ArrayList<>();
        Document doc = XmlUtil.readXML(new java.io.ByteArrayInputStream(
                xml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        NodeList itemNodes = doc.getElementsByTagName("item");
        for (int i = 0; i < itemNodes.getLength(); i++) {
            Element el = (Element) itemNodes.item(i);
            NewsItem item = new NewsItem();
            item.setTitle(XmlUtil.elementText(el, "title"));
            item.setUrl(XmlUtil.elementText(el, "link"));
            item.setSummary(XmlUtil.elementText(el, "description"));
            item.setSource("HNRSS");
            String pubDate = XmlUtil.elementText(el, "pubDate");
            item.setPublishedAt(pubDate == null ? "" : pubDate.trim());
            items.add(item);
        }
        return items;
    }

    // ===== 关键字 / 标签匹配 =====

    private List<NewsItem> filterByKeyword(List<NewsItem> all, String keyword) {
        String kw = keyword.toLowerCase();
        List<NewsItem> result = new ArrayList<>();
        for (NewsItem item : all) {
            String title = item.getTitle() == null ? "" : item.getTitle().toLowerCase();
            String summary = item.getSummary() == null ? "" : item.getSummary().toLowerCase();
            if (title.contains(kw) || summary.contains(kw)) {
                result.add(item);
            }
        }
        return result;
    }

    private String matchAnyTag(NewsItem item, String[] tags) {
        String title = item.getTitle() == null ? "" : item.getTitle().toLowerCase();
        String summary = item.getSummary() == null ? "" : item.getSummary().toLowerCase();
        for (String tag : tags) {
            String t = tag.toLowerCase();
            if (title.contains(t) || summary.contains(t)) {
                return tag;
            }
        }
        return null;
    }

    // ===== 缓存 =====

    private record CacheEntry(long timestamp, List<NewsItem> data) {}

    private List<NewsItem> getCache(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) return null;
        if (System.currentTimeMillis() - entry.timestamp() > CACHE_TTL_MS) {
            cache.remove(key);
            return null;
        }
        return entry.data();
    }

    private void putCache(String key, List<NewsItem> data) {
        cache.put(key, new CacheEntry(System.currentTimeMillis(), data));
    }
}
