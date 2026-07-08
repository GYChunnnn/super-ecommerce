package com.javastudy.ecommerce.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 队列/交换机/绑定配置 —— 死信队列实现订单超时取消
 *
 * 流程：
 *   下单 → 发送消息到 order.ttl.queue（30s TTL）
 *        → 超时后自动路由到 order.dlx.queue
 *        → 消费者检查订单状态，未支付则取消
 */
@Configuration
public class RabbitMqQueueConfig {

    // ==================== 交换机 ====================
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_DLX_EXCHANGE = "order.dlx.exchange";

    // ==================== 队列 ====================
    public static final String ORDER_TTL_QUEUE = "order.ttl.queue";
    public static final String ORDER_DLX_QUEUE = "order.dlx.queue";
    public static final String STOCK_ALERT_QUEUE = "stock.alert.queue";

    // ==================== 路由键 ====================
    public static final String ORDER_TTL_KEY = "order.ttl";
    public static final String ORDER_DLX_KEY = "order.dlx";
    public static final String STOCK_ALERT_KEY = "stock.alert";
    public static final String SECKILL_ORDER_KEY = "seckill.order";
    public static final String SECKILL_ORDER_QUEUE = "seckill.order.queue";

    // ==================== 消息 TTL ====================
    public static final long ORDER_TTL = 30_000L; // 30 秒演示用

    // ==================== Bean 定义 ====================

    @Bean
    DirectExchange orderDlxExchange() {
        return new DirectExchange(ORDER_DLX_EXCHANGE);
    }

    @Bean
    Queue orderDlxQueue() {
        return QueueBuilder.durable(ORDER_DLX_QUEUE).build();
    }

    @Bean
    Binding orderDlxBinding() {
        return BindingBuilder.bind(orderDlxQueue()).to(orderDlxExchange()).with(ORDER_DLX_KEY);
    }

    @Bean
    DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    @Bean
    Queue orderTtlQueue() {
        return QueueBuilder.durable(ORDER_TTL_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ORDER_DLX_KEY)
                .withArgument("x-message-ttl", ORDER_TTL)
                .build();
    }

    @Bean
    Binding orderTtlBinding() {
        return BindingBuilder.bind(orderTtlQueue()).to(orderExchange()).with(ORDER_TTL_KEY);
    }

    @Bean
    Queue stockAlertQueue() {
        return QueueBuilder.durable(STOCK_ALERT_QUEUE).build();
    }

    @Bean
    Binding stockAlertBinding() {
        return BindingBuilder.bind(stockAlertQueue()).to(orderExchange()).with(STOCK_ALERT_KEY);
    }

    // ==================== 秒杀异步下单 ====================

    @Bean
    Queue seckillOrderQueue() {
        return QueueBuilder.durable(SECKILL_ORDER_QUEUE).build();
    }

    @Bean
    Binding seckillOrderBinding() {
        return BindingBuilder.bind(seckillOrderQueue()).to(orderExchange()).with(SECKILL_ORDER_KEY);
    }
}
