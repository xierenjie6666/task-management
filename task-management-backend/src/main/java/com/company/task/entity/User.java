package com.company.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体（导师 / 实习生）。
 */
@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** 密码（BCrypt 加密），不返回给前端 */
    @JsonIgnore
    private String password;

    private String name;

    /** 角色：MENTOR 导师 / INTERN 实习生 */
    private String role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
