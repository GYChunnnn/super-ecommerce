package com.javastudy.ecommerce.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.javastudy.ecommerce.module.user.model.dto.UserLoginRequest;
import com.javastudy.ecommerce.module.user.model.dto.UserLoginResponse;
import com.javastudy.ecommerce.module.user.model.dto.UserRegisterRequest;
import com.javastudy.ecommerce.module.user.model.entity.User;

public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    void register(UserRegisterRequest request);

    /**
     * 用户登录
     */
    UserLoginResponse login(UserLoginRequest request);
}
