package com.sakay.ecommerce.service;

import com.sakay.ecommerce.dto.response.PaymentResponse;

import java.util.Map;
import java.util.UUID;

public interface PaymentService {
    PaymentResponse createPaymentLink(String email, UUID orderId);
    void handleWebhook(Map<String, Object> payload);
    PaymentResponse getPaymentStatus(UUID orderId);
}
