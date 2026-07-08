package com.javastudy.ecommerce.module.product.controller;

import com.javastudy.ecommerce.common.result.Result;
import com.javastudy.ecommerce.module.product.model.dto.CategoryRequest;
import com.javastudy.ecommerce.module.product.model.entity.Category;
import com.javastudy.ecommerce.module.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商品分类", description = "商品分类增删改查")
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final ProductService productService;

    @Operation(summary = "查询所有分类")
    @GetMapping
    public Result<List<Category>> list() {
        return Result.success(productService.listCategories());
    }

    @Operation(summary = "创建分类")
    @PostMapping
    public Result<Category> create(@Valid @RequestBody CategoryRequest request) {
        return Result.success(productService.createCategory(request));
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public Result<Category> update(@PathVariable Long id,
                                   @Valid @RequestBody CategoryRequest request) {
        return Result.success(productService.updateCategory(id, request));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.deleteCategory(id);
        return Result.success();
    }
}
