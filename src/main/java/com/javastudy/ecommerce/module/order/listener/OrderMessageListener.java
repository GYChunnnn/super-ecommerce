package com.javastudy.ecommerce.module.order.listener;

import com.javastudy.ecommerce.module.order.model.dto.OrderCancelMessage;
import com.javastudy.ecommerce.module.order.model.dto.StockAlertMessage;
import com.javastudy.ecommerce.module.order.service.OrderService;
import com.javastudy.ecommerce.module.order.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 消息消费者
 *
 * 幂等性保证：
 *   用 Redis 记录已处理的消息 ID（SETNX），
 *   如果消息已处理过（key 存在），直接跳过。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMessageListener {

    private final OrderService orderService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String IDEMPOTENT_PREFIX = "msg:consumed:";
    private static final long IDEMPOTENT_TTL = 7; // 天

    // ==================== 订单超时取消消费 ====================

    @RabbitListener(queues = "order.dlx.queue")
    public void handleOrderCancel(OrderCancelMessage message) {
        // 幂等性检查
        String idempotentKey = IDEMPOTENT_PREFIX + message.getMessageId();
        Boolean isFirst = redisTemplate.opsForValue()
                .setIfAbsent(idempotentKey, "1", IDEMPOTENT_TTL, TimeUnit.DAYS);
        if (Boolean.FALSE.equals(isFirst)) {
            log.warn("消息已消费过，跳过: messageId={}", message.getMessageId());
            return;
        }

        log.info("收到订单超时消息: orderId={}, messageId={}", message.getOrderId(), message.getMessageId());
        try {
            orderService.cancelOrder(message.getUserId(), message.getOrderId());
            log.info("订单超时取消成功: orderId={}", message.getOrderId());
        } catch (Exception e) {
            log.warn("订单超时取消失败(可能已支付): orderId={}, reason={}",
                    message.getOrderId(), e.getMessage());
        }
    }

    // ==================== 库存预警消费 ====================

    @RabbitListener(queues = "stock.alert.queue")
    public void handleStockAlert(StockAlertMessage message) {
        String idempotentKey = IDEMPOTENT_PREFIX + message.getMessageId();
        Boolean isFirst = redisTemplate.opsForValue()
                .setIfAbsent(idempotentKey, "1", IDEMPOTENT_TTL, TimeUnit.DAYS);
        if (Boolean.FALSE.equals(isFirst)) {
            log.warn("库存预警消息已处理，跳过: messageId={}", message.getMessageId());
            return;
        }

        log.warn("【库存预警】商品[{}](id={}) 库存不足，当前库存: {}",
                message.getProductName(), message.getProductId(), message.getCurrentStock());
        // TODO: 实际项目此处可发送短信/邮件/企业内部通知
    }
}
