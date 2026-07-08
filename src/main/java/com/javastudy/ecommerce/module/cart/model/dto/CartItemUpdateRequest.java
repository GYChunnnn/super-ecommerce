package com.javastudy.ecommerce.module.cart.model.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 更新购物车请求（数量/选中状态）
 */
@Data
public class CartItemUpdateRequest {

    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;

    /** 是否选中：0-未选 1-选中 */
    private Integer selected;
}
