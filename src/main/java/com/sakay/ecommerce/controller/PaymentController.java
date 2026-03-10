package com.sakay.ecommerce.controller;

import com.sakay.ecommerce.dto.response.PaymentResponse;
import com.sakay.ecommerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-link")
    public ResponseEntity<PaymentResponse> createPaymentLink(@AuthenticationPrincipal UserDetails userDetails,
                                                              @RequestParam UUID orderId) {
        return ResponseEntity.ok(paymentService.createPaymentLink(userDetails.getUsername(), orderId));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody Map<String, Object> payload) {
        paymentService.handleWebhook(payload);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<PaymentResponse> getStatus(@PathVariable UUID orderId) {
        return ResponseEntity.ok(paymentService.getPaymentStatus(orderId));
    }
}
