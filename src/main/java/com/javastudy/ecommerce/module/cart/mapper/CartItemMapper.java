package com.javastudy.ecommerce.module.cart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javastudy.ecommerce.module.cart.model.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车 Mapper
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
}
