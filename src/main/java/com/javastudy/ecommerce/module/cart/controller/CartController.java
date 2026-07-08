package com.javastudy.ecommerce.module.cart.controller;

import com.javastudy.ecommerce.common.result.Result;
import com.javastudy.ecommerce.config.SecurityContextUtil;
import com.javastudy.ecommerce.module.cart.model.dto.CartItemAddRequest;
import com.javastudy.ecommerce.module.cart.model.dto.CartItemUpdateRequest;
import com.javastudy.ecommerce.module.cart.model.vo.CartItemVO;
import com.javastudy.ecommerce.module.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "购物车", description = "购物车增删改查、全选/清空")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "查询购物车列表")
    @GetMapping
    public Result<List<CartItemVO>> list() {
        return Result.success(cartService.list(SecurityContextUtil.getCurrentUserId()));
    }

    @Operation(summary = "添加商品到购物车")
    @PostMapping
    public Result<CartItemVO> add(@Valid @RequestBody CartItemAddRequest request) {
        return Result.success(cartService.add(SecurityContextUtil.getCurrentUserId(), request));
    }

    @Operation(summary = "修改数量/选中状态")
    @PutMapping("/{id}")
    public Result<CartItemVO> update(@PathVariable("id") Long cartItemId,
                                     @RequestBody CartItemUpdateRequest request) {
        return Result.success(cartService.update(SecurityContextUtil.getCurrentUserId(), cartItemId, request));
    }

    @Operation(summary = "删除购物车项")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long cartItemId) {
        cartService.delete(SecurityContextUtil.getCurrentUserId(), cartItemId);
        return Result.success();
    }

    @Operation(summary = "全选/取消全选")
    @PutMapping("/select-all")
    public Result<Void> selectAll(@RequestParam Integer selected) {
        cartService.selectAll(SecurityContextUtil.getCurrentUserId(), selected);
        return Result.success();
    }

    @Operation(summary = "清空购物车")
    @DeleteMapping
    public Result<Void> clear() {
        cartService.clear(SecurityContextUtil.getCurrentUserId());
        return Result.success();
    }
}
