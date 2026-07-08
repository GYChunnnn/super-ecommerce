package com.javastudy.ecommerce.module.product.service.impl;

import com.javastudy.ecommerce.common.exception.BusinessException;
import com.javastudy.ecommerce.module.product.mapper.ProductMapper;
import com.javastudy.ecommerce.module.product.model.entity.Product;
import com.javastudy.ecommerce.module.product.service.ProductCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 商品缓存服务实现
 *
 * 解决三大缓存问题：
 * 1. 缓存穿透：缓存空对象（"NULL_PRODUCT"），防止恶意请求穿透到 DB
 * 2. 缓存击穿：互斥锁（synchronized），热点数据过期时只让一个线程查 DB
 * 3. 缓存雪崩：随机过期时间（30min ± 5min），避免大量 key 同时过期
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCacheServiceImpl implements ProductCacheService {

    private final ProductMapper productMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "product:detail:";
    private static final String NULL_PLACEHOLDER = "NULL_PRODUCT";
    private static final long CACHE_TTL_MINUTES = 30;
    private static final long NULL_TTL_MINUTES = 5;
    private final Random random = new Random();

    @Override
    public Product getById(Long id) {
        String key = CACHE_KEY_PREFIX + id;

        // 1. 查缓存
        Object cached = redisTemplate.opsForValue().get(key);

        // 2. 命中空对象 → 穿透防护生效
        if (NULL_PLACEHOLDER.equals(cached)) {
            log.info("缓存穿透防护: 商品[id={}]不存在", id);
            throw new BusinessException("商品不存在");
        }

        // 3. 命中真实数据
        if (cached instanceof Product) {
            log.info("缓存命中: 商品[id={}]", id);
            return (Product) cached;
        }

        // 4. 缓存未命中 → 互斥锁查 DB（击穿防护）
        Product product = queryWithMutexLock(id, key);
        if (product == null) {
            // 5. 缓存空对象（穿透防护）
            cacheNullValue(id, key);
            throw new BusinessException("商品不存在");
        }
        return product;
    }

    @Override
    public void updateCache(Product product) {
        String key = CACHE_KEY_PREFIX + product.getId();
        long ttl = CACHE_TTL_MINUTES + random.nextInt(10); // 随机 TTL（雪崩防护）
        redisTemplate.opsForValue().set(key, product, ttl, TimeUnit.MINUTES);
        log.info("缓存更新: 商品[id={}], TTL={}min", product.getId(), ttl);
    }

    @Override
    public void evictCache(Long id) {
        redisTemplate.delete(CACHE_KEY_PREFIX + id);
        log.info("缓存删除: 商品[id={}]", id);
    }

    // ==================== 私有方法 ====================

    /** 互斥锁查 DB（击穿防护） */
    private synchronized Product queryWithMutexLock(Long id, String key) {
        // 双重检查：获得锁后再查一次
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof Product) {
            return (Product) cached;
        }
        if (NULL_PLACEHOLDER.equals(cached)) {
            return null;
        }

        log.info("缓存未命中，查 DB: 商品[id={}]", id);
        Product product = productMapper.selectById(id);

        if (product != null) {
            long ttl = CACHE_TTL_MINUTES + random.nextInt(10); // 随机 TTL（雪崩防护）
            redisTemplate.opsForValue().set(key, product, ttl, TimeUnit.MINUTES);
        }
        return product;
    }

    /** 缓存空对象（穿透防护） */
    private void cacheNullValue(Long id, String key) {
        log.info("缓存空对象: 商品[id={}]", id);
        redisTemplate.opsForValue().set(key, NULL_PLACEHOLDER, NULL_TTL_MINUTES, TimeUnit.MINUTES);
    }
}
