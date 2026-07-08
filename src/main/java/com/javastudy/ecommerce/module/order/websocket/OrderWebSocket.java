package com.javastudy.ecommerce.module.order.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 订单状态推送
 *
 * 客户端连接: ws://localhost:8080/ws/order?userId=1
 * 服务端推送订单状态变更通知
 */
@Slf4j
@Component
@ServerEndpoint("/ws/order")
public class OrderWebSocket {

    /** userId → session 映射 */
    private static final Map<Long, Session> CLIENT_SESSIONS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        String userId = getUserId(session);
        if (userId != null) {
            CLIENT_SESSIONS.put(Long.parseLong(userId), session);
            log.info("WebSocket 连接: userId={}, sessionId={}", userId, session.getId());
        }
    }

    @OnClose
    public void onClose(Session session) {
        String userId = getUserId(session);
        if (userId != null) {
            CLIENT_SESSIONS.remove(Long.parseLong(userId));
            log.info("WebSocket 断开: userId={}", userId);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 异常: {}", error.getMessage());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到客户端消息: {}", message);
    }

    /** 推送订单状态更新通知 */
    public static void pushOrderStatus(Long userId, String orderNo, String statusDesc) {
        Session session = CLIENT_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String msg = String.format("{\"type\":\"order\",\"orderNo\":\"%s\",\"status\":\"%s\"}",
                        orderNo, statusDesc);
                session.getBasicRemote().sendText(msg);
                log.info("WebSocket 推送: userId={}, orderNo={}, status={}", userId, orderNo, statusDesc);
            } catch (IOException e) {
                log.error("WebSocket 推送失败: {}", e.getMessage());
                CLIENT_SESSIONS.remove(userId);
            }
        }
    }

    private String getUserId(Session session) {
        String query = session.getQueryString();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=");
                if (kv.length == 2 && "userId".equals(kv[0])) {
                    return kv[1];
                }
            }
        }
        return null;
    }
}
