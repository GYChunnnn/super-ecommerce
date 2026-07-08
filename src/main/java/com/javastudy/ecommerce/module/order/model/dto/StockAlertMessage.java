package com.javastudy.ecommerce.module.order.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 库存预警消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockAlertMessage implements Serializable {

    private Long productId;
    private String productName;
    private int currentStock;
    private String messageId;
}
