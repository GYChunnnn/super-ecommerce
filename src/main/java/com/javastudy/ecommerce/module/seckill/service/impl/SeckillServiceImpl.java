package com.javastudy.ecommerce.module.seckill.service.impl;

import com.javastudy.ecommerce.module.product.mapper.ProductMapper;
import com.javastudy.ecommerce.module.product.model.entity.Product;
import com.javastudy.ecommerce.module.product.service.ProductCacheService;
import com.javastudy.ecommerce.module.seckill.model.dto.SeckillOrderMessage;
import com.javastudy.ecommerce.module.seckill.model.dto.SeckillResult;
import com.javastudy.ecommerce.module.seckill.service.SeckillService;
import com.javastudy.ecommerce.config.RabbitMqQueueConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀服务实现 —— MQ 异步创建订单
 * <p>
 * 核心流程：
 * 1. Redisson 分布式锁，防止超卖
 * 2. 同步扣减库存（快速返回，不阻塞）
 * 3. 发送 MQ 消息异步创建订单
 * 4. 订单创建成功 → 更新缓存
 * <p>
 * 相比同步创建订单的优势：
 * - 扣库存操作极快（UPDATE SQL），抢购完成立即返回
 * - 订单创建是慢操作（INSERT 订单主表 + 明细表），走 MQ 异步不拖累响应
 * - MQ 削峰：大量秒杀请求被队列平滑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private final ProductMapper productMapper;
    private final ProductCacheService productCacheService;
    private final RedissonClient redissonClient;
    private final RabbitTemplate rabbitTemplate;

    private static final String LOCK_KEY_PREFIX = "seckill:lock:";

    @Override
    @Transactional
    public SeckillResult seckill(Long userId, Long productId, Integer quantity) {
        String lockKey = LOCK_KEY_PREFIX + productId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 1. 尝试加锁（等待 3 秒，锁 10 秒自动释放）
            boolean acquired = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("秒杀加锁失败: userId={}, productId={}", userId, productId);
                return SeckillResult.fail("抢购人数过多，请稍后再试");
            }

            // 2. 查库存
            Product product = productMapper.selectById(productId);
            if (product == null || product.getStatus() == 0) {
                return SeckillResult.fail("商品不存在或已下架");
            }
            if (product.getStock() < quantity) {
                log.info("库存不足: productId={}, 剩余={}, 请求={}", productId, product.getStock(), quantity);
                return SeckillResult.fail("库存不足，当前库存: " + product.getStock());
            }

            // 3. 预扣库存（同步，极快）
            product.setStock(product.getStock() - quantity);
            product.setSales(product.getSales() + quantity);
            productMapper.updateById(product);

            // 4. 更新缓存
            productCacheService.updateCache(product);

            // 5. 发送 MQ 消息异步创建订单（释放锁后由消费者处理）
            String messageId = productId + "-" + userId + "-" + UUID.randomUUID().toString().substring(0, 8);
            SeckillOrderMessage msg = new SeckillOrderMessage();
            msg.setUserId(userId);
            msg.setProductId(productId);
            msg.setProductName(product.getName());
            msg.setProductImage(product.getMainImage());
            msg.setPrice(product.getPrice());
            msg.setQuantity(quantity);
            msg.setMessageId(messageId);

            rabbitTemplate.convertAndSend(
                    RabbitMqQueueConfig.ORDER_EXCHANGE,
                    RabbitMqQueueConfig.SECKILL_ORDER_KEY,
                    msg
            );

            log.info("秒杀库存扣减成功，订单消息已发送: userId={}, productId={}, quantity={}, messageId={}",
                    userId, productId, quantity, messageId);
            // 返回 success:true 但 orderId 为 null，前端提示"订单处理中"
            return SeckillResult.ok(null);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return SeckillResult.fail("系统繁忙");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
