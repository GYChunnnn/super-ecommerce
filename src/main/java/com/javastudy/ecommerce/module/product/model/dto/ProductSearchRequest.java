package com.javastudy.ecommerce.module.product.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * ES 商品搜索结果请求
 */
@Data
public class ProductSearchRequest {

    /** 搜索关键词 */
    private String keyword;

    /** 分类ID */
    private Long categoryId;

    /** 最低价格 */
    private BigDecimal priceMin;

    /** 最高价格 */
    private BigDecimal priceMax;

    /** 排序方式：sales(销量) / price(价格) / _score(相关度) */
    private String sortBy;

    /** ASC / DESC */
    private String sortDir;

    /** 页码 */
    private Integer page = 1;

    /** 每页条数 */
    private Integer size = 10;
}
