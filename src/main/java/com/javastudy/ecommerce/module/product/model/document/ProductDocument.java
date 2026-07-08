package com.javastudy.ecommerce.module.product.model.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ES 商品文档 —— 用于搜索
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocument {

    private String id;          // 商品ID
    private Long categoryId;    // 分类ID
    private String name;        // 商品名称
    private String description; // 商品简介
    private BigDecimal price;   // 价格
    private Integer stock;      // 库存
    private Integer sales;      // 销量
    private String mainImage;   // 主图
    private Integer status;     // 状态
}
