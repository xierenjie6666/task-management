package com.company.task.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.task.config.CurrentUser;
import com.company.task.dto.TaskRequest;
import com.company.task.dto.TaskVO;
import com.company.task.entity.Task;
import com.company.task.exception.BusinessException;
import com.company.task.mapper.TaskMapper;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 任务服务：CRUD、状态流转、筛选、仪表盘统计。
 *
 * <p>状态流转规则：TODO -> IN_PROGRESS -> DONE，允许回退。</p>
 * <p>权限：实习生只能操作分配给自己或自己创建的任务；导师可操作全部。</p>
 */
@Service
public class TaskService {

    /** 合法状态 */
    private static final Set<String> VALID_STATUS =
            Set.of("TODO", "IN_PROGRESS", "DONE");
    /** 合法优先级 */
    private static final Set<String> VALID_PRIORITY =
            Set.of("HIGH", "MEDIUM", "LOW");

    private final TaskMapper taskMapper;

    public TaskService(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    /**
     * 多条件查询任务（带权限过滤）。
     */
    public List<TaskVO> list(String keyword, String status, Long assigneeId,
                             String priority, Long creatorId) {
        CurrentUser.LoginUser cur = CurrentUser.get();
        if (cur == null) {
            throw new BusinessException(401, "未登录");
        }
        return taskMapper.selectTaskList(keyword, status, assigneeId, priority,
                creatorId, cur.userId(), cur.role());
    }

    public TaskVO getById(Long id) {
        TaskVO vo = taskMapper.selectTaskById(id);
        if (vo == null) {
            throw new BusinessException(404, "任务不存在");
        }
        // 实习生权限校验
        CurrentUser.LoginUser cur = CurrentUser.get();
        if (!cur.isMentor()
                && !cur.userId().equals(vo.getAssigneeId())
                && !cur.userId().equals(vo.getCreatorId())) {
            throw new BusinessException(403, "无权查看该任务");
        }
        return vo;
    }

    public TaskVO create(TaskRequest req) {
        CurrentUser.LoginUser cur = CurrentUser.get();
        Task task = new Task();
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setStatus(req.getStatus() == null || req.getStatus().isBlank() ? "TODO" : req.getStatus().toUpperCase());
        task.setPriority(req.getPriority() == null || req.getPriority().isBlank() ? "MEDIUM" : req.getPriority().toUpperCase());
        task.setAssigneeId(req.getAssigneeId());
        task.setCreatorId(cur.userId());
        task.setDueDate(req.getDueDate());
        task.setTags(req.getTags());
        validateStatus(task.getStatus());
        validatePriority(task.getPriority());
        taskMapper.insert(task);
        return taskMapper.selectTaskById(task.getId());
    }

    public TaskVO update(Long id, TaskRequest req) {
        Task exist = taskMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(404, "任务不存在");
        }
        checkPermission(exist);
        if (req.getTitle() != null) exist.setTitle(req.getTitle());
        if (req.getDescription() != null) exist.setDescription(req.getDescription());
        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            validateStatus(req.getStatus().toUpperCase());
            exist.setStatus(req.getStatus().toUpperCase());
        }
        if (req.getPriority() != null && !req.getPriority().isBlank()) {
            validatePriority(req.getPriority().toUpperCase());
            exist.setPriority(req.getPriority().toUpperCase());
        }
        if (req.getAssigneeId() != null) exist.setAssigneeId(req.getAssigneeId());
        if (req.getDueDate() != null) exist.setDueDate(req.getDueDate());
        if (req.getTags() != null) exist.setTags(req.getTags());
        taskMapper.updateById(exist);
        return taskMapper.selectTaskById(id);
    }

    /**
     * 状态流转（便捷接口）。
     */
    public TaskVO changeStatus(Long id, String newStatus) {
        Task exist = taskMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(404, "任务不存在");
        }
        checkPermission(exist);
        validateStatus(newStatus.toUpperCase());
        exist.setStatus(newStatus.toUpperCase());
        taskMapper.updateById(exist);
        return taskMapper.selectTaskById(id);
    }

    public void delete(Long id) {
        Task exist = taskMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(404, "任务不存在");
        }
        checkPermission(exist);
        taskMapper.deleteById(id);
    }

    /**
     * 仪表盘统计：按状态分组计数 + 完成率 + 即将到期任务数。
     */
    public Map<String, Object> dashboard() {
        CurrentUser.LoginUser cur = CurrentUser.get();
        List<TaskVO> tasks = taskMapper.selectTaskList(null, null, null, null, null,
                cur.userId(), cur.role());

        Map<String, Long> statusCount = new HashMap<>();
        statusCount.put("TODO", 0L);
        statusCount.put("IN_PROGRESS", 0L);
        statusCount.put("DONE", 0L);
        for (TaskVO t : tasks) {
            statusCount.merge(t.getStatus(), 1L, Long::sum);
        }
        long total = tasks.size();
        long done = statusCount.get("DONE");

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("todo", statusCount.get("TODO"));
        result.put("inProgress", statusCount.get("IN_PROGRESS"));
        result.put("done", done);
        result.put("completionRate", total == 0 ? 0 : Math.round(done * 100.0 / total));
        result.put("tasks", tasks);
        return result;
    }

    // ===== private =====

    private void validateStatus(String status) {
        if (!VALID_STATUS.contains(status)) {
            throw new BusinessException("状态非法，仅支持 TODO / IN_PROGRESS / DONE");
        }
    }

    private void validatePriority(String priority) {
        if (!VALID_PRIORITY.contains(priority)) {
            throw new BusinessException("优先级非法，仅支持 HIGH / MEDIUM / LOW");
        }
    }

    private void checkPermission(Task task) {
        CurrentUser.LoginUser cur = CurrentUser.get();
        if (cur.isMentor()) {
            return;
        }
        if (!cur.userId().equals(task.getAssigneeId()) && !cur.userId().equals(task.getCreatorId())) {
            throw new BusinessException(403, "无权操作该任务");
        }
    }
}
