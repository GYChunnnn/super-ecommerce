package com.javastudy.ecommerce.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.javastudy.ecommerce.common.exception.BusinessException;
import com.javastudy.ecommerce.module.user.mapper.UserMapper;
import com.javastudy.ecommerce.module.user.model.dto.UserLoginRequest;
import com.javastudy.ecommerce.module.user.model.dto.UserLoginResponse;
import com.javastudy.ecommerce.module.user.model.dto.UserRegisterRequest;
import com.javastudy.ecommerce.module.user.model.entity.User;
import com.javastudy.ecommerce.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void register(UserRegisterRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        long count = this.count(wrapper);
        BusinessException.throwIf(count > 0, "用户名已存在");

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(1);
        this.save(user);
    }

    @Override
    public UserLoginResponse login(UserLoginRequest request) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = this.getOne(wrapper);
        BusinessException.throwIfNull(user, "用户名或密码错误");

        // 校验密码
        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查状态
        if (user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用");
        }

        // TODO: 生成 JWT Token（第二阶段实现）
        String token = "eyJ_algo_placeholder_token_" + user.getId();

        return new UserLoginResponse(token, user.getId(), user.getUsername(), user.getNickname());
    }
}
