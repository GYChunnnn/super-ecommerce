package com.javastudy.ecommerce.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javastudy.ecommerce.common.result.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器 —— 每个请求前解析 Token 并设置认证上下文
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token == null) {
            // 无 Token，继续走（给 permitAll 的接口）
            filterChain.doFilter(request, response);
            return;
        }

        // Token 过期
        if (jwtUtil.isTokenExpired(token)) {
            sendError(response, 401, "Token 已过期，请重新登录");
            return;
        }

        // Token 无效
        if (!jwtUtil.validateToken(token)) {
            sendError(response, 401, "Token 无效");
            return;
        }

        // 设置认证上下文
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,  // principal → userId
                        null,    // credentials
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
        authentication.setDetails(username); // details → username

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void sendError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.error(code, message);
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}
