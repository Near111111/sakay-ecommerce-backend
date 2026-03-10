package com.sakay.ecommerce.service.impl;

import com.sakay.ecommerce.entity.Order;
import com.sakay.ecommerce.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOrderConfirmation(Order order) {
        send(order.getUser().getEmail(),
                "Order Confirmed - " + order.getOrderNumber(),
                "Your order " + order.getOrderNumber() + " has been confirmed. Total: ₱" + order.getTotalAmount());
    }

    @Override
    public void sendCancelNotification(Order order) {
        send(order.getUser().getEmail(),
                "Order Cancelled - " + order.getOrderNumber(),
                "Your order " + order.getOrderNumber() + " has been cancelled.");
    }

    @Override
    public void sendPaymentReceipt(Order order) {
        send(order.getUser().getEmail(),
                "Payment Receipt - " + order.getOrderNumber(),
                "Payment received for order " + order.getOrderNumber() + ". Amount: ₱" + order.getTotalAmount());
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
