package com.javastudy.ecommerce.module.product.controller;

import com.javastudy.ecommerce.common.result.Result;
import com.javastudy.ecommerce.module.product.model.dto.ProductSearchRequest;
import com.javastudy.ecommerce.module.product.service.ProductSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商品搜索接口（Elasticsearch）
 */
@Tag(name = "商品搜索", description = "ES 商品搜索（关键词高亮、分类/价格筛选、排序）")
@RestController
@RequestMapping("/api/product/search")
@RequiredArgsConstructor
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    @Operation(summary = "ES 搜索商品")
    @GetMapping
    public Result<Map<String, Object>> search(ProductSearchRequest request) {
        return Result.success(productSearchService.search(request));
    }

    @Operation(summary = "全量同步 MySQL → ES")
    @PostMapping("/sync")
    public Result<String> fullSync() {
        productSearchService.fullSync();
        return Result.success("同步完成");
    }
}
