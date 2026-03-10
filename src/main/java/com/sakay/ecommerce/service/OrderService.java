package com.sakay.ecommerce.service;

import com.sakay.ecommerce.dto.request.CheckoutRequest;
import com.sakay.ecommerce.dto.response.OrderResponse;
import com.sakay.ecommerce.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {
    OrderResponse checkout(String email, CheckoutRequest request);
    Page<OrderResponse> getOrders(String email, Pageable pageable);
    OrderResponse getOrderById(String email, UUID orderId);
    OrderResponse cancelOrder(String email, UUID orderId);
    OrderResponse updateStatus(UUID orderId, Order.OrderStatus status);
}
