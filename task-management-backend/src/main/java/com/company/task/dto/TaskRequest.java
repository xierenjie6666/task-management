package com.company.task.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务创建/编辑请求。
 */
@Data
public class TaskRequest {

    @NotBlank(message = "任务标题不能为空")
    private String title;

    private String description;

    /** 状态：TODO / IN_PROGRESS / DONE，创建时可不传，默认 TODO */
    private String status;

    /** 优先级：HIGH / MEDIUM / LOW，默认 MEDIUM */
    private String priority;

    private Long assigneeId;

    private LocalDateTime dueDate;

    /** 标签（逗号分隔） */
    private String tags;
}
