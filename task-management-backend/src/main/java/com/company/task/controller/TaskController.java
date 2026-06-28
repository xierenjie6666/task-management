package com.company.task.controller;

import com.company.task.common.Result;
import com.company.task.config.CurrentUser;
import com.company.task.dto.TaskRequest;
import com.company.task.dto.TaskVO;
import com.company.task.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务接口：CRUD + 状态流转 + 筛选 + 仪表盘。
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /** 任务列表（筛选/搜索） */
    @GetMapping
    public Result<List<TaskVO>> list(@RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) String status,
                                     @RequestParam(required = false) Long assigneeId,
                                     @RequestParam(required = false) String priority,
                                     @RequestParam(required = false) Long creatorId) {
        return Result.success(taskService.list(keyword, status, assigneeId, priority, creatorId));
    }

    @GetMapping("/{id}")
    public Result<TaskVO> getById(@PathVariable Long id) {
        return Result.success(taskService.getById(id));
    }

    @PostMapping
    public Result<TaskVO> create(@Valid @RequestBody TaskRequest req) {
        return Result.success(taskService.create(req));
    }

    @PutMapping("/{id}")
    public Result<TaskVO> update(@PathVariable Long id, @RequestBody TaskRequest req) {
        return Result.success(taskService.update(id, req));
    }

    /** 状态流转（拖拽/按钮切换） */
    @PatchMapping("/{id}/status")
    public Result<TaskVO> changeStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return Result.success(taskService.changeStatus(id, body.get("status")));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return Result.success();
    }

    /** 个人仪表盘统计 */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        return Result.success(taskService.dashboard());
    }
}
