package com.sakay.ecommerce.service;

import com.sakay.ecommerce.entity.Order;

public interface SmsService {
    void sendOrderConfirmation(Order order, String phone);
    void sendCancelNotification(Order order, String phone);
    void sendPaymentReceipt(Order order, String phone);
}