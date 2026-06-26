package com.javastudy.ecommerce.module.user.controller;

import com.javastudy.ecommerce.common.result.Result;
import com.javastudy.ecommerce.module.user.model.dto.UserLoginRequest;
import com.javastudy.ecommerce.module.user.model.dto.UserLoginResponse;
import com.javastudy.ecommerce.module.user.model.dto.UserRegisterRequest;
import com.javastudy.ecommerce.module.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody UserRegisterRequest request) {
        userService.register(request);
        return Result.success(null);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        UserLoginResponse response = userService.login(request);
        return Result.success(response);
    }
}
