package com.javastudy.ecommerce.module.order.service.impl;

import com.javastudy.ecommerce.config.RabbitMqQueueConfig;
import com.javastudy.ecommerce.module.order.model.dto.OrderCancelMessage;
import com.javastudy.ecommerce.module.order.model.dto.StockAlertMessage;
import com.javastudy.ecommerce.module.order.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 消息服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendOrderDelayCancelMessage(Long orderId, Long userId) {
        String messageId = orderId + "-" + UUID.randomUUID().toString().substring(0, 8);
        OrderCancelMessage message = new OrderCancelMessage(orderId, userId, messageId);

        rabbitTemplate.convertAndSend(
                RabbitMqQueueConfig.ORDER_EXCHANGE,
                RabbitMqQueueConfig.ORDER_TTL_KEY,
                message
        );
        log.info("发送订单超时消息: orderId={}, messageId={}", orderId, messageId);
    }

    @Override
    public void sendStockAlertMessage(Long productId, String productName, int currentStock) {
        String messageId = productId + "-" + UUID.randomUUID().toString().substring(0, 8);
        StockAlertMessage message = new StockAlertMessage(productId, productName, currentStock, messageId);

        rabbitTemplate.convertAndSend(
                RabbitMqQueueConfig.ORDER_EXCHANGE,
                RabbitMqQueueConfig.STOCK_ALERT_KEY,
                message
        );
        log.info("发送库存预警消息: productId={}, name={}, stock={}", productId, productName, currentStock);
    }
}
