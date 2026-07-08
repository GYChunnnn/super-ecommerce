package com.javastudy.ecommerce.module.cart.model.vo;

import com.javastudy.ecommerce.module.product.model.entity.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车展示对象（含商品信息）
 */
@Data
public class CartItemVO {

    private Long id;
    private Long productId;
    private String productName;
    private String mainImage;
    private BigDecimal price;
    private Integer stock;
    private Integer quantity;
    private Integer selected;
    private LocalDateTime createTime;
}
