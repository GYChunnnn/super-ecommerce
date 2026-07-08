package com.javastudy.ecommerce.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.javastudy.ecommerce.module.user.mapper.UserMapper;
import com.javastudy.ecommerce.module.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 初始化管理员账户（密码: admin123）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void run(String... args) {
        // 物理删除逻辑删除的旧 admin（绕过 @TableLogic）
        userMapper.deleteAdminForce("admin");

        User exist = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, "admin")
                        .last("LIMIT 1")
        );
        if (exist != null) {
            exist.setPassword(bCryptPasswordEncoder.encode("admin123"));
            userMapper.updateById(exist);
            log.info("✅ 管理员密码已重置: admin / admin123");
        } else {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(bCryptPasswordEncoder.encode("admin123"));
            admin.setNickname("管理员");
            admin.setStatus(1);
            userMapper.insert(admin);
            log.info("✅ 管理员账户初始化完成: admin / admin123");
        }
    }
}
