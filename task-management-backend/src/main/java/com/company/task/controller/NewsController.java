package com.company.task.controller;

import com.company.task.common.Result;
import com.company.task.dto.NewsItem;
import com.company.task.service.NewsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 资讯接口：获取最新资讯、按标签关联、搜索。
 *
 * <p>对应需求"实时资讯模块"：调用 Hacker News API + RSS，支持任务关联。</p>
 */
@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    /** 获取最新资讯（支持 keyword 搜索），公开接口无需登录 */
    @GetMapping
    public Result<List<NewsItem>> latest(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Integer size) {
        return Result.success(newsService.getLatestNews(keyword, size));
    }

    /** 按任务标签关联获取资讯（任务详情/表单使用） */
    @GetMapping("/by-tags")
    public Result<List<NewsItem>> byTags(@RequestParam String tags,
                                         @RequestParam(required = false) Integer size) {
        return Result.success(newsService.getNewsByTags(tags, size));
    }
}
