package com.javastudy.ecommerce.module.order.service;

import com.javastudy.ecommerce.module.order.model.dto.OrderCancelMessage;
import com.javastudy.ecommerce.module.order.model.dto.StockAlertMessage;

/**
 * 消息服务 —— 发送/处理 RabbitMQ 消息
 */
public interface MessageService {

    /** 发送订单超时消息（下单时调用） */
    void sendOrderDelayCancelMessage(Long orderId, Long userId);

    /** 发送库存预警消息 */
    void sendStockAlertMessage(Long productId, String productName, int currentStock);
}
