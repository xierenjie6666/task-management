-- 内部任务管理系统 数据库初始化脚本
-- MySQL 8.0
-- AI 辅助说明：本 SQL 由 AI 工具协助生成

DROP DATABASE IF EXISTS task_db;
CREATE DATABASE task_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE task_db;

-- ----------------------------
-- 用户表（导师 / 实习生）
-- ----------------------------
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL COMMENT '登录用户名',
    password    VARCHAR(50)  NOT NULL COMMENT '密码（明文存储，仅数字字母）',
    name        VARCHAR(50)  NOT NULL COMMENT '显示名称',
    role        VARCHAR(20)  NOT NULL DEFAULT 'INTERN' COMMENT '角色：MENTOR 导师 / INTERN 实习生',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 任务表
-- ----------------------------
DROP TABLE IF EXISTS tasks;
CREATE TABLE tasks (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title        VARCHAR(200) NOT NULL COMMENT '任务标题',
    description  TEXT         COMMENT '任务描述',
    status       VARCHAR(20)  NOT NULL DEFAULT 'TODO' COMMENT '状态：TODO 待办 / IN_PROGRESS 进行中 / DONE 已完成',
    priority     VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM' COMMENT '优先级：HIGH 高 / MEDIUM 中 / LOW 低',
    assignee_id  BIGINT       COMMENT '负责人 ID',
    creator_id   BIGINT       NOT NULL COMMENT '创建人 ID',
    due_date     DATETIME     COMMENT '截止日期',
    tags         VARCHAR(200) COMMENT '标签（逗号分隔，用于关联资讯，如 Java,Spring Boot）',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_assignee (assignee_id),
    KEY idx_status (status),
    KEY idx_creator (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- ----------------------------
-- 初始用户数据由后端 DataInitializer 自动生成（密码明文存储，仅数字字母）
-- 默认账号（密码均为 123456）：
--   mentor   / 123456  （导师，id=1）
--   intern01 / 123456  （实习生，id=2）
--   intern02 / 123456  （实习生，id=3）
-- ----------------------------

-- 初始任务示例
INSERT INTO tasks (title, description, status, priority, assignee_id, creator_id, due_date, tags) VALUES
('搭建 Spring Boot 项目骨架', '初始化后端项目，配置 MyBatis-Plus 与 MySQL', 'DONE', 'HIGH', 2, 1, '2026-06-28 18:00:00', 'Java,Spring Boot'),
('完成登录页 UI', '使用 Vue3 + Element Plus 实现登录页面', 'IN_PROGRESS', 'HIGH', 3, 1, '2026-06-30 18:00:00', 'Vue,前端'),
('编写任务 CRUD 接口', '实现任务的增删改查 RESTful API', 'TODO', 'HIGH', 2, 1, '2026-07-01 18:00:00', 'Java,Spring Boot'),
('集成 Echarts 统计图表', '仪表盘展示任务完成率饼图与柱状图', 'TODO', 'MEDIUM', 3, 1, '2026-07-02 18:00:00', 'Vue,前端');
