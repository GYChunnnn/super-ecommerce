package com.javastudy.ecommerce.module.seckill.model.dto;

import lombok.Data;

/**
 * 秒杀结果
 */
@Data
public class SeckillResult {

    /** 订单ID，null 表示秒杀失败 */
    private Long orderId;

    /** 是否成功 */
    private boolean success;

    /** 提示信息 */
    private String message;

    public static SeckillResult ok(Long orderId) {
        SeckillResult r = new SeckillResult();
        r.success = true;
        r.orderId = orderId;
        r.message = "秒杀成功";
        return r;
    }

    public static SeckillResult fail(String message) {
        SeckillResult r = new SeckillResult();
        r.success = false;
        r.message = message;
        return r;
    }
}
