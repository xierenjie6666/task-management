package com.company.task.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.task.config.JwtUtil;
import com.company.task.dto.LoginRequest;
import com.company.task.dto.LoginResponse;
import com.company.task.dto.RegisterRequest;
import com.company.task.entity.User;
import com.company.task.exception.BusinessException;
import com.company.task.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
 * 认证服务：登录、注册（密码明文存储与比对，仅数字字母）。
 */
@Service
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public AuthService(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest req) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername()));
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        // 明文比对（不加密）
        if (!req.getPassword().equals(user.getPassword())) {
            throw new BusinessException(401, "密码错误");
        }
        String token = jwtUtil.generate(user.getId(), user.getUsername(), user.getName(), user.getRole());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getName(), user.getRole());
    }

    public LoginResponse register(RegisterRequest req) {
        Long exists = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername()));
        if (exists != null && exists > 0) {
            throw new BusinessException("用户名已存在");
        }
        String role = (req.getRole() == null || req.getRole().isBlank()) ? "INTERN" : req.getRole().toUpperCase();
        if (!"MENTOR".equals(role) && !"INTERN".equals(role)) {
            throw new BusinessException("角色非法，仅支持 MENTOR / INTERN");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        // 密码明文存储（仅数字字母，不加密）
        user.setPassword(req.getPassword());
        user.setName(req.getName());
        user.setRole(role);
        userMapper.insert(user);

        String token = jwtUtil.generate(user.getId(), user.getUsername(), user.getName(), user.getRole());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getName(), user.getRole());
    }
}
