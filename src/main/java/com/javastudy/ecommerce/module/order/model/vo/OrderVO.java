package com.javastudy.ecommerce.module.order.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.javastudy.ecommerce.module.order.model.entity.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情 VO（含明细）
 */
@Data
public class OrderVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private Integer status;
    private String statusDesc;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;

    /** 订单明细 */
    private List<OrderItem> items;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    public static String getStatusDesc(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待支付";
            case 1 -> "已支付";
            case 2 -> "已发货";
            case 3 -> "已完成";
            case 4 -> "已取消";
            default -> "未知";
        };
    }
}
