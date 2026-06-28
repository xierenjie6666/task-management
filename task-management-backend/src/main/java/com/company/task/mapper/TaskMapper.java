package com.company.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.task.dto.TaskVO;
import com.company.task.entity.Task;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskMapper extends BaseMapper<Task> {

    /**
     * 根据多条件查询任务（含负责人/创建人姓名）。
     *
     * @param keyword      标题/描述模糊关键字（可空）
     * @param status       状态（可空）
     * @param assigneeId   负责人 ID（可空）
     * @param priority     优先级（可空）
     * @param creatorId    创建人 ID（可空，用于实习生只看自己创建的）
     * @param viewerId     查看人 ID（实习生只能看分配给自己或自己创建的）
     * @param viewerRole   查看人角色
     */
    List<TaskVO> selectTaskList(@Param("keyword") String keyword,
                                @Param("status") String status,
                                @Param("assigneeId") Long assigneeId,
                                @Param("priority") String priority,
                                @Param("creatorId") Long creatorId,
                                @Param("viewerId") Long viewerId,
                                @Param("viewerRole") String viewerRole);

    TaskVO selectTaskById(@Param("id") Long id);
}
