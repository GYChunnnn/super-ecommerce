package com.javastudy.ecommerce.module.cart.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 添加购物车请求
 */
@Data
public class CartItemAddRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Min(value = 1, message = "数量至少为1")
    private Integer quantity = 1;
}
