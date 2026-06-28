package com.company.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务实体。
 */
@Data
@TableName("tasks")
public class Task {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String description;

    /** 状态：TODO / IN_PROGRESS / DONE */
    private String status;

    /** 优先级：HIGH / MEDIUM / LOW */
    private String priority;

    private Long assigneeId;
    private Long creatorId;
    private LocalDateTime dueDate;

    /** 标签（逗号分隔，用于关联资讯，如 Java,Spring Boot） */
    private String tags;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
