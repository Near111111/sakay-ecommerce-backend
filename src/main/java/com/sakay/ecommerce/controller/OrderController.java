package com.sakay.ecommerce.controller;

import com.sakay.ecommerce.dto.request.CheckoutRequest;
import com.sakay.ecommerce.dto.response.OrderResponse;
import com.sakay.ecommerce.entity.Order;
import com.sakay.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> checkout(@AuthenticationPrincipal UserDetails userDetails,
                                                   @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(orderService.checkout(userDetails.getUsername(), request));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrders(@AuthenticationPrincipal UserDetails userDetails,
                                                          Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrders(userDetails.getUsername(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@AuthenticationPrincipal UserDetails userDetails,
                                                  @PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(userDetails.getUsername(), id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@AuthenticationPrincipal UserDetails userDetails,
                                                 @PathVariable UUID id) {
        return ResponseEntity.ok(orderService.cancelOrder(userDetails.getUsername(), id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable UUID id,
                                                       @RequestParam Order.OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }
}
