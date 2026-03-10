package com.sakay.ecommerce.service;

import com.sakay.ecommerce.dto.request.CartItemRequest;

import java.util.Map;
import java.util.UUID;

public interface CartService {
    Map<String, Object> getCart(UUID userId);
    Map<String, Object> addItem(UUID userId, CartItemRequest request);
    Map<String, Object> updateItem(UUID userId, String itemKey, int qty);
    void removeItem(UUID userId, String itemKey);
    void clearCart(UUID userId);
}
