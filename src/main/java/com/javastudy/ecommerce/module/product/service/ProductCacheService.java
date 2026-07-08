package com.javastudy.ecommerce.module.product.service;

import com.javastudy.ecommerce.module.product.model.entity.Product;

/**
 * 商品缓存服务 —— 处理缓存穿透、击穿、雪崩
 */
public interface ProductCacheService {

    /** 根据 ID 查询（带缓存） */
    Product getById(Long id);

    /** 更新缓存 */
    void updateCache(Product product);

    /** 删除缓存 */
    void evictCache(Long id);
}
