package com.sakay.ecommerce.service;

import com.sakay.ecommerce.entity.Order;

public interface EmailService {
    void sendOrderConfirmation(Order order, String email);
    void sendCancelNotification(Order order, String email);
    void sendPaymentReceipt(Order order, String email);
}