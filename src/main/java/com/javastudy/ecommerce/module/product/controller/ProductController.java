package com.javastudy.ecommerce.module.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.javastudy.ecommerce.common.result.Result;
import com.javastudy.ecommerce.module.product.model.dto.ProductCreateRequest;
import com.javastudy.ecommerce.module.product.model.dto.ProductQueryRequest;
import com.javastudy.ecommerce.module.product.model.dto.ProductUpdateRequest;
import com.javastudy.ecommerce.module.product.model.entity.Product;
import com.javastudy.ecommerce.module.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商品管理", description = "商品增删改查、分页搜索")
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "分页查询商品（支持模糊搜索、价格筛选、排序）")
    @GetMapping
    public Result<Page<Product>> query(ProductQueryRequest request) {
        return Result.success(productService.queryProducts(request));
    }

    @Operation(summary = "查询商品详情")
    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.success(productService.getProductById(id));
    }

    @Operation(summary = "创建商品")
    @PostMapping
    public Result<Product> create(@Valid @RequestBody ProductCreateRequest request) {
        return Result.success(productService.createProduct(request));
    }

    @Operation(summary = "更新商品")
    @PutMapping("/{id}")
    public Result<Product> update(@PathVariable Long id,
                                  @RequestBody ProductUpdateRequest request) {
        return Result.success(productService.updateProduct(id, request));
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success();
    }
}
