package com.company.task.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务展示对象（含负责人/创建人姓名，便于前端直接渲染）。
 */
@Data
public class TaskVO {

    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Long assigneeId;
    private String assigneeName;
    private Long creatorId;
    private String creatorName;
    private LocalDateTime dueDate;
    private String tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
