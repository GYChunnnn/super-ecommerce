package com.javastudy.ecommerce.module.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.javastudy.ecommerce.module.order.model.dto.OrderCreateRequest;
import com.javastudy.ecommerce.module.order.model.vo.OrderVO;

import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService {

    /** 创建订单（从购物车已选商品） */
    OrderVO createOrder(Long userId, OrderCreateRequest request);

    /** 支付订单 */
    void payOrder(Long userId, Long orderId);

    /** 取消订单 */
    void cancelOrder(Long userId, Long orderId);

    /** 用户订单列表（分页） */
    Page<OrderVO> listOrders(Long userId, Integer page, Integer size);

    /** 订单详情 */
    OrderVO getOrderDetail(Long userId, Long orderId);

    /** 仪表盘统计 */
    Map<String, Object> getDashboardStats();

    /** 热门商品排行 */
    List<Map<String, Object>> getHotProducts(int limit);

    /** 近7天销售趋势 */
    List<Map<String, Object>> getSalesTrend();
}
