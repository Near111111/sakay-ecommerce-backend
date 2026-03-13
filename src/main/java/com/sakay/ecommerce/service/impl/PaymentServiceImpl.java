package com.sakay.ecommerce.service.impl;

import com.sakay.ecommerce.dto.response.PaymentResponse;
import com.sakay.ecommerce.entity.Order;
import com.sakay.ecommerce.entity.Payment;
import com.sakay.ecommerce.exception.ResourceNotFoundException;
import com.sakay.ecommerce.repository.OrderRepository;
import com.sakay.ecommerce.repository.PaymentRepository;
import com.sakay.ecommerce.service.SmsService;
import com.sakay.ecommerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final SmsService smsService;

    @Value("${paymongo.secret-key}")
    private String paymongoSecretKey;

    private static final String PAYMONGO_BASE = "https://api.paymongo.com/v1";

    @Override
    @Transactional
    public PaymentResponse createPaymentLink(String email, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // If a payment link already exists and is still pending, reuse it
        Optional<Payment> existing = paymentRepository.findByOrderId(orderId);
        if (existing.isPresent()
                && existing.get().getStatus() == Payment.PaymentStatus.PENDING
                && existing.get().getCheckoutUrl() != null) {
            log.info("Reusing existing payment link for order {}", order.getOrderNumber());
            return PaymentResponse.from(existing.get());
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = buildHeaders();

            Map<String, Object> body = Map.of(
                    "data", Map.of("attributes", Map.of(
                            "amount", order.getTotalAmount().multiply(java.math.BigDecimal.valueOf(100)).intValue(),
                            "currency", "PHP",
                            "description", "Payment for order " + order.getOrderNumber()
                    ))
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(PAYMONGO_BASE + "/links", entity, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");

            Payment payment = Payment.builder()
                    .order(order)
                    .paymongoLinkId(data.get("id").toString())
                    .amount(order.getTotalAmount())
                    .checkoutUrl(attributes.get("checkout_url").toString())
                    .build();

            return PaymentResponse.from(paymentRepository.save(payment));
        } catch (Exception e) {
            log.error("PayMongo createLink failed for order {}: {}", order.getOrderNumber(), e.getMessage());
            throw new RuntimeException("Failed to create payment link. Please try again.");
        }
    }

    @Override
    @Transactional
    public void handleWebhook(Map<String, Object> payload) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            @SuppressWarnings("unchecked")
            Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
            String type = attributes.get("type").toString();

            if ("link.payment.paid".equals(type)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> linkData = (Map<String, Object>) attributes.get("data");
                String linkId = linkData.get("id").toString();

                paymentRepository.findByPaymongoLinkId(linkId).ifPresent(payment -> {
                    payment.setStatus(Payment.PaymentStatus.PAID);
                    payment.setPaidAt(LocalDateTime.now());
                    paymentRepository.save(payment);

                    Order order = payment.getOrder();
                    order.setStatus(Order.OrderStatus.CONFIRMED);
                    orderRepository.save(order);

                    String userPhone = order.getUser() != null ? order.getUser().getPhone() : null;
                    if (userPhone != null) {
                        smsService.sendPaymentReceipt(order, userPhone);
                    } else {
                        log.warn("Could not send payment receipt SMS - phone not available for order {}", order.getOrderNumber());
                    }
                });
            }
        } catch (Exception e) {
            log.error("Webhook handling failed: {}", e.getMessage());
        }
    }

    @Override
    public PaymentResponse getPaymentStatus(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        return PaymentResponse.from(payment);
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String encoded = Base64.getEncoder().encodeToString((paymongoSecretKey + ":").getBytes());
        headers.set("Authorization", "Basic " + encoded);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}