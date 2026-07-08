package com.javastudy.ecommerce.module.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javastudy.ecommerce.module.product.model.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品 Mapper
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
