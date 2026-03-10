package com.sakay.ecommerce.dto.response;

import com.sakay.ecommerce.entity.Payment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class PaymentResponse {
    private UUID id;
    private UUID orderId;
    private Payment.PaymentStatus status;
    private Payment.PaymentMethod method;
    private BigDecimal amount;
    private String checkoutUrl;
    private String paidAt;

    public static PaymentResponse from(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .orderId(p.getOrder().getId())
                .status(p.getStatus())
                .method(p.getMethod())
                .amount(p.getAmount())
                .checkoutUrl(p.getCheckoutUrl())
                .paidAt(p.getPaidAt() != null ? p.getPaidAt().toString() : null)
                .build();
    }
}