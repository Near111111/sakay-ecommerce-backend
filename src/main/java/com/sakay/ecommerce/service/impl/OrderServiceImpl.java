package com.sakay.ecommerce.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sakay.ecommerce.dto.request.CheckoutRequest;
import com.sakay.ecommerce.dto.response.OrderResponse;
import com.sakay.ecommerce.entity.*;
import com.sakay.ecommerce.exception.BadRequestException;
import com.sakay.ecommerce.exception.ResourceNotFoundException;
import com.sakay.ecommerce.repository.*;
import com.sakay.ecommerce.service.CartService;
import com.sakay.ecommerce.service.EmailService;
import com.sakay.ecommerce.service.OrderService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final CartService cartService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    @Override
    @Transactional
    @SneakyThrows
    public OrderResponse checkout(String email, CheckoutRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        BigDecimal shippingFee = request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.valueOf(100);
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        boolean hasDirectItems = request.getItems() != null && !request.getItems().isEmpty();

        if (hasDirectItems) {
            // Direct checkout — items from request body
            for (CheckoutRequest.CheckoutItemRequest itemReq : request.getItems()) {
                Product product = productRepository.findById(itemReq.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.getProductId()));

                BigDecimal price = product.getBasePrice();
                String variantLabel = null;
                UUID variantId = null;

                if (itemReq.getVariantId() != null) {
                    ProductVariant variant = variantRepository.findById(itemReq.getVariantId())
                            .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));
                    if (variant.getStockQty() < itemReq.getQuantity()) {
                        throw new BadRequestException("Insufficient stock for: " + product.getName());
                    }
                    price = price.add(variant.getPriceModifier());
                    variantLabel = variant.getVariantType() + ": " + variant.getVariantValue();
                    variantId = variant.getId();
                }

                BigDecimal total = price.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
                subtotal = subtotal.add(total);

                orderItems.add(OrderItem.builder()
                        .productId(product.getId())
                        .productVariantId(variantId)
                        .productName(product.getName())
                        .variantLabel(variantLabel)
                        .qty(itemReq.getQuantity())
                        .unitPrice(price)
                        .totalPrice(total)
                        .build());
            }
        } else {
            // Cart-based checkout — read from Redis
            Map<String, Object> cart = cartService.getCart(user.getId());
            @SuppressWarnings("unchecked")
            Map<Object, Object> items = (Map<Object, Object>) cart.get("items");

            if (items == null || items.isEmpty()) {
                throw new BadRequestException("Cart is empty");
            }

            for (Map.Entry<Object, Object> entry : items.entrySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> item = (Map<String, Object>) entry.getValue();
                BigDecimal price = new BigDecimal(item.get("price").toString());
                int qty = Integer.parseInt(item.get("qty").toString());
                BigDecimal total = price.multiply(BigDecimal.valueOf(qty));
                subtotal = subtotal.add(total);

                orderItems.add(OrderItem.builder()
                        .productId(UUID.fromString(item.get("productId").toString()))
                        .productVariantId(item.get("variantId") != null ? UUID.fromString(item.get("variantId").toString()) : null)
                        .productName(item.get("name").toString())
                        .variantLabel(item.get("variantLabel") != null ? item.get("variantLabel").toString() : null)
                        .qty(qty)
                        .unitPrice(price)
                        .totalPrice(total)
                        .build());
            }

            // Only clear cart on cart-based checkout
            cartService.clearCart(user.getId());
        }

        String orderNumber = "SAK-" + System.currentTimeMillis();
        String addressSnapshot = String.format("%s, %s, %s, %s, %s %s",
                address.getStreet(),
                address.getBarangay() != null ? address.getBarangay() : "",
                address.getCity(),
                address.getProvince(),
                address.getZipCode(),
                address.getRegion() != null ? address.getRegion() : "").trim();

        Order order = Order.builder()
                .user(user)
                .orderNumber(orderNumber)
                .deliveryAddress(addressSnapshot)
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .totalAmount(subtotal.add(shippingFee))
                .build();

        Order saved = orderRepository.save(order);
        entityManager.flush();
        entityManager.refresh(saved);
        orderItems.forEach(i -> i.setOrder(saved));
        orderItemRepository.saveAll(orderItems);

        // Pass email directly to avoid LazyInitializationException on order.getUser()
        emailService.sendOrderConfirmation(saved, email);

        return OrderResponse.from(saved);
    }

    @Override
    public Page<OrderResponse> getOrders(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return orderRepository.findByUserId(user.getId(), pageable).map(OrderResponse::from);
    }

    @Override
    public OrderResponse getOrderById(String email, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return OrderResponse.from(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(String email, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new BadRequestException("Only PENDING orders can be cancelled");
        }
        order.setStatus(Order.OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        entityManager.flush();
        entityManager.refresh(saved);
        // Pass email directly to avoid LazyInitializationException on order.getUser()
        emailService.sendCancelNotification(saved, email);
        return OrderResponse.from(saved);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(UUID orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(status);
        return OrderResponse.from(orderRepository.save(order));
    }
}