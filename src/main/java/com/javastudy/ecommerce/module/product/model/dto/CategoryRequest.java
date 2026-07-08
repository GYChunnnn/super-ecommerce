package com.javastudy.ecommerce.module.product.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建/更新分类的请求
 */
@Data
public class CategoryRequest {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private Long parentId = 0L;

    private Integer sortOrder = 0;

    private Integer status = 1;
}
