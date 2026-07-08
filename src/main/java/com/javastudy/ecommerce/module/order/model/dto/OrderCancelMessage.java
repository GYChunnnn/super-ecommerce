package com.javastudy.ecommerce.module.order.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单取消消息 —— 通过 RabbitMQ 死信队列投递
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelMessage implements Serializable {

    /** 订单ID */
    private Long orderId;

    /** 用户ID */
    private Long userId;

    /** 消息ID（用于幂等性校验） */
    private String messageId;
}
