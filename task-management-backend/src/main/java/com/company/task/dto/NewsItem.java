package com.company.task.dto;

import lombok.Data;

/**
 * 资讯条目（统一封装第三方来源）。
 */
@Data
public class NewsItem {

    private String title;
    private String url;
    private String source;
    private String summary;
    /** 发布时间（毫秒时间戳或文本） */
    private String publishedAt;
    /** 关联标签（命中任务的哪个标签） */
    private String matchedTag;
}
