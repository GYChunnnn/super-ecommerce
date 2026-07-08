package com.javastudy.ecommerce.module.product.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新商品的请求
 */
@Data
public class ProductUpdateRequest {

    private Long categoryId;

    private String name;

    private String description;

    private String detail;

    private BigDecimal price;

    private Integer stock;

    private String mainImage;

    private String images;

    private Integer status;
}
