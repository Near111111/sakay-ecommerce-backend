package com.sakay.ecommerce.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sakay.ecommerce.dto.request.CartItemRequest;
import com.sakay.ecommerce.entity.Product;
import com.sakay.ecommerce.entity.ProductVariant;
import com.sakay.ecommerce.exception.BadRequestException;
import com.sakay.ecommerce.exception.ResourceNotFoundException;
import com.sakay.ecommerce.repository.ProductRepository;
import com.sakay.ecommerce.repository.ProductVariantRepository;
import com.sakay.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    private static final String CART_PREFIX = "cart:";
    private static final long CART_TTL_HOURS = 72;

    @Override
    public Map<String, Object> getCart(UUID userId) {
        String key = CART_PREFIX + userId;
        Map<Object, Object> rawCart = redisTemplate.opsForHash().entries(key);
        return new HashMap<>(Map.of("items", rawCart, "total", calculateTotal(rawCart)));
    }

    @Override
    public Map<String, Object> addItem(UUID userId, CartItemRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        BigDecimal price = product.getBasePrice();
        String variantLabel = null;

        if (request.getVariantId() != null) {
            ProductVariant variant = variantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));
            if (variant.getStockQty() < request.getQty()) {
                throw new BadRequestException("Insufficient stock");
            }
            price = price.add(variant.getPriceModifier());
            variantLabel = variant.getVariantType() + ": " + variant.getVariantValue();
        }

        String itemKey = request.getProductId() + (request.getVariantId() != null ? ":" + request.getVariantId() : "");
        Map<String, Object> item = new HashMap<>();
        item.put("productId", request.getProductId().toString());
        item.put("variantId", request.getVariantId() != null ? request.getVariantId().toString() : null);
        item.put("name", product.getName());
        item.put("variantLabel", variantLabel);
        item.put("price", price);
        item.put("qty", request.getQty());

        String cartKey = CART_PREFIX + userId;
        redisTemplate.opsForHash().put(cartKey, itemKey, item);
        redisTemplate.expire(cartKey, CART_TTL_HOURS, TimeUnit.HOURS);

        return getCart(userId);
    }

    @Override
    public Map<String, Object> updateItem(UUID userId, String itemKey, int qty) {
        String cartKey = CART_PREFIX + userId;
        Object rawItem = redisTemplate.opsForHash().get(cartKey, itemKey);
        if (rawItem == null) throw new ResourceNotFoundException("Cart item not found");

        @SuppressWarnings("unchecked")
        Map<String, Object> item = (Map<String, Object>) rawItem;
        item.put("qty", qty);
        redisTemplate.opsForHash().put(cartKey, itemKey, item);
        return getCart(userId);
    }

    @Override
    public void removeItem(UUID userId, String itemKey) {
        redisTemplate.opsForHash().delete(CART_PREFIX + userId, itemKey);
    }

    @Override
    public void clearCart(UUID userId) {
        redisTemplate.delete(CART_PREFIX + userId);
    }

    private BigDecimal calculateTotal(Map<Object, Object> rawCart) {
        return rawCart.values().stream()
                .map(v -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> item = (Map<String, Object>) v;
                    BigDecimal price = new BigDecimal(item.get("price").toString());
                    int qty = Integer.parseInt(item.get("qty").toString());
                    return price.multiply(BigDecimal.valueOf(qty));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
