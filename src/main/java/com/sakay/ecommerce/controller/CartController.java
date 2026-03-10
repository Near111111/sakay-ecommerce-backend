package com.sakay.ecommerce.controller;

import com.sakay.ecommerce.dto.request.CartItemRequest;
import com.sakay.ecommerce.entity.User;
import com.sakay.ecommerce.repository.UserRepository;
import com.sakay.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(cartService.getCart(user.getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<Map<String, Object>> addItem(@AuthenticationPrincipal UserDetails userDetails,
                                                        @Valid @RequestBody CartItemRequest request) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(cartService.addItem(user.getId(), request));
    }

    @PutMapping("/items/{itemKey}")
    public ResponseEntity<Map<String, Object>> updateItem(@AuthenticationPrincipal UserDetails userDetails,
                                                           @PathVariable String itemKey,
                                                           @RequestParam int qty) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(cartService.updateItem(user.getId(), itemKey, qty));
    }

    @DeleteMapping("/items/{itemKey}")
    public ResponseEntity<Void> removeItem(@AuthenticationPrincipal UserDetails userDetails,
                                           @PathVariable String itemKey) {
        User user = getUser(userDetails);
        cartService.removeItem(user.getId(), itemKey);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        cartService.clearCart(user.getId());
        return ResponseEntity.noContent().build();
    }

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
    }
}
