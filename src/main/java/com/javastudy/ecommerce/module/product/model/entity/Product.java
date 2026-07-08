package com.javastudy.ecommerce.module.product.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体
 */
@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 分类ID */
    private Long categoryId;

    /** 商品名称 */
    private String name;

    /** 商品简介 */
    private String description;

    /** 商品详情（富文本） */
    private String detail;

    /** 价格 */
    private BigDecimal price;

    /** 库存 */
    private Integer stock;

    /** 销量 */
    private Integer sales;

    /** 主图URL */
    private String mainImage;

    /** 图片列表（JSON数组字符串） */
    private String images;

    /** 状态：0-下架 1-上架 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
