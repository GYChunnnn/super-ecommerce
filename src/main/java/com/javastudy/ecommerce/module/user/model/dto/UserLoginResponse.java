package com.javastudy.ecommerce.module.user.model.dto;

import lombok.Data;

/**
 * 登录响应（含 Token）
 */
@Data
public class UserLoginResponse {

    private String token;
    private Long userId;
    private String username;
    private String nickname;

    public UserLoginResponse(String token, Long userId, String username, String nickname) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
    }
}
