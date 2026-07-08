package com.javastudy.ecommerce.module.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.javastudy.ecommerce.common.result.Result;
import com.javastudy.ecommerce.config.SecurityContextUtil;
import com.javastudy.ecommerce.module.order.model.dto.OrderCreateRequest;
import com.javastudy.ecommerce.module.order.model.vo.OrderVO;
import com.javastudy.ecommerce.module.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "订单管理", description = "订单创建、支付、取消、查询")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "创建订单（从购物车已选商品）")
    @PostMapping
    public Result<OrderVO> create(@Valid @RequestBody OrderCreateRequest request) {
        return Result.success(orderService.createOrder(SecurityContextUtil.getCurrentUserId(), request));
    }

    @Operation(summary = "支付订单")
    @PostMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable("id") Long orderId) {
        orderService.payOrder(SecurityContextUtil.getCurrentUserId(), orderId);
        return Result.success();
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable("id") Long orderId) {
        orderService.cancelOrder(SecurityContextUtil.getCurrentUserId(), orderId);
        return Result.success();
    }

    @Operation(summary = "订单列表")
    @GetMapping
    public Result<Page<OrderVO>> list(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(orderService.listOrders(SecurityContextUtil.getCurrentUserId(), page, size));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable("id") Long orderId) {
        return Result.success(orderService.getOrderDetail(SecurityContextUtil.getCurrentUserId(), orderId));
    }
}
