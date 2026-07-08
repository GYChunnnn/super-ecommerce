package com.javastudy.ecommerce.module.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.javastudy.ecommerce.common.exception.BusinessException;
import com.javastudy.ecommerce.module.cart.mapper.CartItemMapper;
import com.javastudy.ecommerce.module.cart.model.entity.CartItem;
import com.javastudy.ecommerce.module.order.mapper.OrderItemMapper;
import com.javastudy.ecommerce.module.order.mapper.OrderMapper;
import com.javastudy.ecommerce.module.order.model.dto.OrderCreateRequest;
import com.javastudy.ecommerce.module.order.model.entity.Order;
import com.javastudy.ecommerce.module.order.model.entity.OrderItem;
import com.javastudy.ecommerce.module.order.model.vo.OrderVO;
import com.javastudy.ecommerce.module.order.service.MessageService;
import com.javastudy.ecommerce.module.order.service.OrderService;
import com.javastudy.ecommerce.module.order.websocket.OrderWebSocket;
import com.javastudy.ecommerce.module.payment.mapper.PaymentMapper;
import com.javastudy.ecommerce.module.payment.model.entity.Payment;
import com.javastudy.ecommerce.module.product.mapper.ProductMapper;
import com.javastudy.ecommerce.module.product.model.entity.Product;
import com.javastudy.ecommerce.module.product.service.ProductCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 订单服务实现
 *
 * 核心事务流程：创建订单 → 扣减库存 → 增加销量 → 清空购物车
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final PaymentMapper paymentMapper;
    private final ProductCacheService productCacheService;
    private final MessageService messageService;

    @Override
    @Transactional
    public OrderVO createOrder(Long userId, OrderCreateRequest request) {
        // 1. 查询用户购物车中已选中的商品
        List<CartItem> selectedItems = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .eq(CartItem::getSelected, 1)
        );
        BusinessException.throwIf(selectedItems.isEmpty(), "请先选择商品");

        // 2. 批量查询商品
        List<Long> productIds = selectedItems.stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toList());
        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 3. 校验库存 + 计算总金额 + 生成明细
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : selectedItems) {
            Product product = productMap.get(cartItem.getProductId());
            BusinessException.throwIfNull(product, "商品不存在");
            BusinessException.throwIf(product.getStatus() == 0, product.getName() + " 已下架");
            BusinessException.throwIf(product.getStock() < cartItem.getQuantity(),
                    product.getName() + " 库存不足，当前库存：" + product.getStock());

            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());           // 快照
            item.setProductImage(product.getMainImage());     // 快照
            item.setPrice(product.getPrice());                // 快照
            item.setQuantity(cartItem.getQuantity());
            item.setTotalAmount(itemTotal);
            orderItems.add(item);
        }

        // 4. 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(0); // 待支付
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverAddress(request.getReceiverAddress());
        order.setRemark(request.getRemark());
        orderMapper.insert(order);

        // 5. 保存订单明细
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        // 6. 扣减库存 + 增加销量
        for (CartItem cartItem : selectedItems) {
            Product product = productMap.get(cartItem.getProductId());
            product.setStock(product.getStock() - cartItem.getQuantity());
            product.setSales(product.getSales() + cartItem.getQuantity());
            productMapper.updateById(product);
        }

        // 7. 清空已选购物车项
        for (CartItem cartItem : selectedItems) {
            cartItemMapper.deleteById(cartItem.getId());
        }

        // 8. 发送订单超时消息（30s 后未支付自动取消）
        messageService.sendOrderDelayCancelMessage(order.getId(), userId);

        // 9. 刷新商品缓存
        for (Product product : productMap.values()) {
            productCacheService.updateCache(product);
        }

        // 10. 库存预警检查
        for (Product product : productMap.values()) {
            if (product.getStock() <= 5 && product.getStock() > 0) {
                messageService.sendStockAlertMessage(
                        product.getId(), product.getName(), product.getStock());
            }
        }

        log.info("订单创建成功: orderNo={}, userId={}, amount={}", order.getOrderNo(), userId, totalAmount);
        // WebSocket 推送通知
        OrderWebSocket.pushOrderStatus(userId, order.getOrderNo(), "待支付");
        return buildOrderVO(order, orderItems);
    }

    @Override
    @Transactional
    public void payOrder(Long userId, Long orderId) {
        Order order = getOwnOrder(userId, orderId);
        BusinessException.throwIf(order.getStatus() != 0, "订单状态不允许支付");

        // 创建支付记录 → 模拟支付成功
        order.setStatus(1); // 已支付
        orderMapper.updateById(order);

        Payment payment = new Payment();
        payment.setPaymentNo("PAY" + UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase());
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setAmount(order.getTotalAmount());
        payment.setPayMethod(0);    // 模拟支付宝
        payment.setStatus(1);       // 支付成功
        payment.setPaidTime(LocalDateTime.now());
        paymentMapper.insert(payment);

        log.info("支付成功: paymentNo={}, orderNo={}", payment.getPaymentNo(), order.getOrderNo());
        // WebSocket 推送通知
        OrderWebSocket.pushOrderStatus(userId, order.getOrderNo(), "已支付");
    }

    @Override
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order order = getOwnOrder(userId, orderId);
        BusinessException.throwIf(order.getStatus() != 0, "仅待支付订单可取消");

        // 恢复库存
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        );
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                product.setSales(Math.max(0, product.getSales() - item.getQuantity()));
                productMapper.updateById(product);
            }
        }

        order.setStatus(4); // 已取消
        orderMapper.updateById(order);
        log.info("订单已取消: orderNo={}", order.getOrderNo());
        // WebSocket 推送通知
        OrderWebSocket.pushOrderStatus(userId, order.getOrderNo(), "已取消");
    }

    @Override
    public Page<OrderVO> listOrders(Long userId, Integer pageNum, Integer size) {
        Page<Order> page = new Page<>(pageNum, size);
        Page<Order> orderPage = orderMapper.selectPage(page,
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .orderByDesc(Order::getCreateTime)
        );

        Page<OrderVO> result = new Page<>(pageNum, size, orderPage.getTotal());
        List<OrderVO> voList = orderPage.getRecords().stream()
                .map(order -> {
                    List<OrderItem> items = orderItemMapper.selectList(
                            new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId())
                    );
                    return buildOrderVO(order, items);
                })
                .collect(Collectors.toList());
        result.setRecords(voList);
        return result;
    }

    @Override
    public OrderVO getOrderDetail(Long userId, Long orderId) {
        Order order = getOwnOrder(userId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        );
        return buildOrderVO(order, items);
    }

    // ==================== 私有方法 ====================

    private Order getOwnOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        BusinessException.throwIfNull(order, "订单不存在");
        BusinessException.throwIf(!order.getUserId().equals(userId), "无权操作该订单");
        return order;
    }

    private OrderVO buildOrderVO(Order order, List<OrderItem> items) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusDesc(OrderVO.getStatusDesc(order.getStatus()));
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());
        vo.setRemark(order.getRemark());
        vo.setItems(items);
        vo.setCreateTime(order.getCreateTime());
        vo.setUpdateTime(order.getUpdateTime());
        return vo;
    }

    private String generateOrderNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuidPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return datePart + uuidPart;
    }

    // ==================== 数据统计 ====================

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new java.util.LinkedHashMap<>();

        // 今日订单数 & 今日销售额
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        List<Order> todayOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .ge(Order::getCreateTime, todayStart)
                        .in(Order::getStatus, 1, 2, 3) // 有效订单
        );
        stats.put("todayOrderCount", todayOrders.size());
        stats.put("todaySales", todayOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // 总用户数（通过 productMapper 没法查，用一个估算）
        stats.put("totalProducts", productMapper.selectCount(null));

        // 待处理订单
        Long pendingCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, 0));
        stats.put("pendingOrders", pendingCount);

        return stats;
    }

    @Override
    public List<Map<String, Object>> getHotProducts(int limit) {
        return productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 1)
                        .orderByDesc(Product::getSales)
                        .last("LIMIT " + limit)
        ).stream().map(p -> {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("price", p.getPrice());
            map.put("sales", p.getSales());
            map.put("stock", p.getStock());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getSalesTrend() {
        List<Map<String, Object>> result = new ArrayList<>();
        // 近 7 天每天的销售额
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            List<Order> dayOrders = orderMapper.selectList(
                    new LambdaQueryWrapper<Order>()
                            .ge(Order::getCreateTime, dayStart)
                            .lt(Order::getCreateTime, dayEnd)
                            .in(Order::getStatus, 1, 2, 3) // 已支付的订单
            );

            Map<String, Object> day = new java.util.LinkedHashMap<>();
            day.put("date", date.toString());
            day.put("orderCount", dayOrders.size());
            day.put("totalAmount", dayOrders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            result.add(day);
        }
        return result;
    }
}
