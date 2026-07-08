package com.javastudy.ecommerce.module.product.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * ES 搜索结果
 */
@Data
public class ProductSearchResult {

    /** 商品ID */
    private String id;

    /** 商品名称 */
    private String name;

    /** 商品名称高亮片段 */
    private String nameHighlight;

    /** 商品简介高亮片段 */
    private String descHighlight;

    /** 分类ID */
    private Long categoryId;

    /** 价格 */
    private BigDecimal price;

    /** 库存 */
    private Integer stock;

    /** 销量 */
    private Integer sales;

    /** 主图 */
    private String mainImage;

    /** 相关度评分 */
    private Double score;

    /** ES 返回的原始字段 */
    private Map<String, Object> source;
}
