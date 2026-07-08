package com.javastudy.ecommerce.module.order.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建订单请求
 */
@Data
public class OrderCreateRequest {

    /** 收货人 */
    @NotBlank(message = "收货人不能为空")
    private String receiverName;

    /** 收货电话 */
    @NotBlank(message = "收货电话不能为空")
    private String receiverPhone;

    /** 收货地址 */
    @NotBlank(message = "收货地址不能为空")
    private String receiverAddress;

    /** 备注 */
    private String remark;
}
