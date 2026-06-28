package com.company.task.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.task.entity.User;
import com.company.task.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器：启动时确保默认账号存在（密码明文存储，仅数字字母）。
 *
 * <p>默认账号（密码统一 123456）：
 * <ul>
 *   <li>mentor / 123456 （导师，id=1）</li>
 *   <li>intern01 / 123456 （实习生，id=2）</li>
 *   <li>intern02 / 123456 （实习生，id=3）</li>
 * </ul>
 * 已存在则跳过，保证可重复执行。</p>
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;

    public DataInitializer(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void run(String... args) {
        // 密码明文存储（仅数字字母，不加密）
        String plainPwd = "123456";
        seed("mentor", "张导师", "MENTOR", plainPwd);
        seed("intern01", "李实习", "INTERN", plainPwd);
        seed("intern02", "王实习", "INTERN", plainPwd);
        log.info("默认账号初始化完成（已存在则跳过）");
    }

    private void seed(String username, String name, String role, String plainPwd) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (count != null && count > 0) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(plainPwd);
        user.setName(name);
        user.setRole(role);
        userMapper.insert(user);
        log.info("创建默认账号: {} ({})", username, role);
    }
}
