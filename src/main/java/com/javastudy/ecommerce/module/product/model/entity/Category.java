package com.javastudy.ecommerce.module.product.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品分类实体
 */
@Data
@TableName("category")
public class Category {

    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 分类名称 */
    private String name;

    /** 父分类ID，0表示顶级分类 */
    private Long parentId;

    /** 排序值，越小越靠前 */
    private Integer sortOrder;

    /** 状态：0-禁用 1-正常 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
