package com.javastudy.ecommerce.module.seckill.listener;

import com.javastudy.ecommerce.module.order.mapper.OrderItemMapper;
import com.javastudy.ecommerce.module.order.mapper.OrderMapper;
import com.javastudy.ecommerce.module.order.model.entity.Order;
import com.javastudy.ecommerce.module.order.model.entity.OrderItem;
import com.javastudy.ecommerce.module.product.service.ProductCacheService;
import com.javastudy.ecommerce.module.seckill.model.dto.SeckillOrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀订单消费者 —— 异步创建订单
 *
 * 优势：
 * 1. 削峰：高并发秒杀请求被 MQ 队列平滑处理
 * 2. 解耦：扣库存和创建订单分离，扣库存极快返回
 * 3. 幂等：Redis SETNX 防重复消费
 * 4. 重试：MQ 自动重试，异常消息可进入死信
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillOrderListener {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductCacheService productCacheService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String IDEMPOTENT_PREFIX = "msg:consumed:";

    @RabbitListener(queues = "seckill.order.queue")
    @Transactional
    public void handleSeckillOrder(SeckillOrderMessage msg) {
        // 幂等性检查
        String idempotentKey = IDEMPOTENT_PREFIX + msg.getMessageId();
        Boolean isFirst = redisTemplate.opsForValue()
                .setIfAbsent(idempotentKey, "1", 7, TimeUnit.DAYS);
        if (Boolean.FALSE.equals(isFirst)) {
            log.warn("秒杀订单消息已消费过，跳过: messageId={}", msg.getMessageId());
            return;
        }

        log.info("开始异步创建秒杀订单: userId={}, productId={}, quantity={}, messageId={}",
                msg.getUserId(), msg.getProductId(), msg.getQuantity(), msg.getMessageId());

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(msg.getUserId());
        order.setTotalAmount(msg.getPrice().multiply(BigDecimal.valueOf(msg.getQuantity())));
        order.setStatus(1); // 秒杀直接已支付
        order.setReceiverName("秒杀用户");
        order.setReceiverPhone("");
        order.setReceiverAddress("");
        orderMapper.insert(order);

        // 创建订单明细
        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductId(msg.getProductId());
        item.setProductName(msg.getProductName());
        item.setProductImage(msg.getProductImage());
        item.setPrice(msg.getPrice());
        item.setQuantity(msg.getQuantity());
        item.setTotalAmount(order.getTotalAmount());
        orderItemMapper.insert(item);

        log.info("秒杀订单异步创建成功: orderId={}, orderNo={}, userId={}, productId={}",
                order.getId(), order.getOrderNo(), msg.getUserId(), msg.getProductId());
    }

    private String generateOrderNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuidPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "SK" + datePart + uuidPart;
    }
}
