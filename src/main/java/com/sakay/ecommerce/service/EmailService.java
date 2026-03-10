package com.sakay.ecommerce.service;

import com.sakay.ecommerce.entity.Order;

public interface EmailService {
    void sendOrderConfirmation(Order order);
    void sendCancelNotification(Order order);
    void sendPaymentReceipt(Order order);
}
