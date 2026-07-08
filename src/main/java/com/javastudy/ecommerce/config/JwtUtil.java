package com.javastudy.ecommerce.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expiration;
    private final long refreshExpiration;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expiration,
                   @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }

    /** 生成访问 Token */
    public String generateToken(Long userId, String username) {
        return buildToken(userId, username, expiration);
    }

    /** 生成刷新 Token */
    public String generateRefreshToken(Long userId, String username) {
        return buildToken(userId, username, refreshExpiration);
    }

    /** 从 Token 解析 userId */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /** 从 Token 解析 username */
    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }

    /** 验证 Token 是否有效 */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            log.warn("JWT 验证失败: {}", e.getMessage());
            return false;
        }
    }

    /** 判断 Token 是否过期 */
    public boolean isTokenExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    // ==================== 私有方法 ====================

    private String buildToken(Long userId, String username, long ttl) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttl);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
