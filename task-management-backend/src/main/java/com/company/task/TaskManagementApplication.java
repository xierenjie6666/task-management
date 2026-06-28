package com.company.task;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 内部任务管理系统后端启动类。
 * 技术栈：Spring Boot 3.2.5 + MyBatis-Plus + MySQL + JWT。
 *
 * <p>AI 辅助说明：本项目代码由 AI 工具（Trae / Claude）协助生成骨架与实现，
 * 业务逻辑、SQL 设计、资讯模块对接均由 AI 依据需求文档产出。</p>
 */
@SpringBootApplication
@MapperScan("com.company.task.mapper")
public class TaskManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskManagementApplication.class, args);
        System.out.println("\n========== 任务管理系统后端启动成功 ==========");
        System.out.println("后端地址: http://localhost:8080");
        System.out.println("默认账号: mentor / 123456 (导师) | intern01 / 123456 | intern02 / 123456");
        System.out.println("=============================================\n");
    }
}
