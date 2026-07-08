package com.javastudy.ecommerce.module.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javastudy.ecommerce.module.payment.model.entity.Payment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付记录 Mapper
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}
