package com.javastudy.ecommerce.module.product.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建商品的请求
 */
@Data
public class ProductCreateRequest {

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String description;

    private String detail;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    private Integer stock = 0;

    private String mainImage;

    private String images;

    private Integer status = 1;
}
