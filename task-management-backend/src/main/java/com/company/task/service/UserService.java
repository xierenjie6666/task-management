package com.company.task.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.task.entity.User;
import com.company.task.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务：用户列表（导师分配任务时选择负责人）。
 */
@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public List<User> listAll() {
        return userMapper.selectList(new LambdaQueryWrapper<User>().orderByAsc(User::getId));
    }

    public User getById(Long id) {
        return userMapper.selectById(id);
    }
}
