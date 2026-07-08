package com.javastudy.ecommerce.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javastudy.ecommerce.module.order.model.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细 Mapper
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
