package com.sakay.ecommerce.dto.response;

import com.sakay.ecommerce.entity.Order;
import com.sakay.ecommerce.entity.OrderItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private List<OrderItemDto> items;

    @Data
    @Builder
    public static class OrderItemDto {
        private UUID productId;
        private String productName;
        private String variantLabel;
        private Integer qty;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }

    public static OrderResponse from(Order o) {
        List<OrderItemDto> itemDtos = o.getItems() != null
                ? o.getItems().stream().map(i -> OrderItemDto.builder()
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .variantLabel(i.getVariantLabel())
                        .qty(i.getQty())
                        .unitPrice(i.getUnitPrice())
                        .totalPrice(i.getTotalPrice())
                        .build())
                .collect(Collectors.toList())
                : List.of();

        return OrderResponse.builder()
                .id(o.getId())
                .orderNumber(o.getOrderNumber())
                .status(o.getStatus())
                .deliveryAddress(o.getDeliveryAddress())
                .subtotal(o.getSubtotal())
                .shippingFee(o.getShippingFee())
                .totalAmount(o.getTotalAmount())
                .createdAt(o.getCreatedAt() != null ? o.getCreatedAt().toString() : null)
                .items(itemDtos)
                .build();
    }
}