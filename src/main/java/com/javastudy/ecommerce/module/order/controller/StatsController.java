package com.javastudy.ecommerce.module.order.controller;

import com.javastudy.ecommerce.common.result.Result;
import com.javastudy.ecommerce.module.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据统计接口
 */
@Tag(name = "数据统计", description = "热门商品排行、销售趋势、仪表盘")
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final OrderService orderService;

    @Operation(summary = "仪表盘摘要")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> stats = orderService.getDashboardStats();
        return Result.success(stats);
    }

    @Operation(summary = "热门商品 Top10")
    @GetMapping("/hot-products")
    public Result<List<Map<String, Object>>> hotProducts(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(orderService.getHotProducts(limit));
    }

    @Operation(summary = "近7天销售趋势")
    @GetMapping("/sales-trend")
    public Result<List<Map<String, Object>>> salesTrend() {
        return Result.success(orderService.getSalesTrend());
    }
}
