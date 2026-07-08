package com.javastudy.ecommerce.module.seckill.service;

import com.javastudy.ecommerce.module.seckill.model.dto.SeckillResult;

/**
 * 秒杀服务接口
 */
public interface SeckillService {

    /** 秒杀下单 */
    SeckillResult seckill(Long userId, Long productId, Integer quantity);
}
