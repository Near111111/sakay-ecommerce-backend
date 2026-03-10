package com.sakay.ecommerce.dto.response;

import com.sakay.ecommerce.entity.Order;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {
    private UUID id;
    private String orderNumber;
    private Order.OrderStatus status;
    private String deliveryAddress;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String createdAt;

    public static OrderResponse from(Order o) {
        return OrderResponse.builder()
                .id(o.getId())
                .orderNumber(o.getOrderNumber())
                .status(o.getStatus())
                .deliveryAddress(o.getDeliveryAddress())
                .subtotal(o.getSubtotal())
                .shippingFee(o.getShippingFee())
                .totalAmount(o.getTotalAmount())
                .createdAt(o.getCreatedAt() != null ? o.getCreatedAt().toString() : null)
                .build();
    }
}