package com.sakay.ecommerce.service.impl;

import com.sakay.ecommerce.entity.Order;
import com.sakay.ecommerce.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Value("${txtbox.api-key}")
    private String apiKey;

    @Override
    public void sendOrderConfirmation(Order order, String phone) {
        String message = "Salamat sa iyong order! Order #" + order.getOrderNumber() +
                " ay nakumpirma na. Total: P" + order.getTotalAmount() +
                ". Abangan ang iyong delivery!";
        send(phone, message);
    }

    @Override
    public void sendCancelNotification(Order order, String phone) {
        String message = "Ang iyong order #" + order.getOrderNumber() +
                " ay nakansela na. Para sa katanungan, makipag-ugnayan sa aming support.";
        send(phone, message);
    }

    @Override
    public void sendPaymentReceipt(Order order, String phone) {
        String message = "Natanggap na ang iyong bayad para sa order #" + order.getOrderNumber() +
                ". Halaga: P" + order.getTotalAmount() + ". Salamat!";
        send(phone, message);
    }

    private void send(String to, String message) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-TXTBOX-Auth", apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("number", to);
            body.put("message", message);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://ws-v2.txtbox.com/messaging/v1/sms/push", request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to send SMS to {}: {}", to, response.getBody());
            } else {
                log.info("SMS sent to {}: {}", to, response.getBody());
            }
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", to, e.getMessage());
        }
    }
}