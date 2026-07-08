package com.javastudy.ecommerce.module.product.model.dto;

import lombok.Data;

/**
 * 商品分页查询请求
 */
@Data
public class ProductQueryRequest {

    /** 分类ID（可选） */
    private Long categoryId;

    /** 模糊搜索关键词（名称） */
    private String keyword;

    /** 最低价格（可选） */
    private Integer priceMin;

    /** 最高价格（可选） */
    private Integer priceMax;

    /** 排序字段：price / sales / create_time */
    private String sortBy;

    /** 排序方向：ASC / DESC */
    private String sortDir;

    /** 页码，默认1 */
    private Integer page = 1;

    /** 每页条数，默认10 */
    private Integer size = 10;
}
