package com.sakay.ecommerce.controller;

import com.sakay.ecommerce.dto.request.AddressRequest;
import com.sakay.ecommerce.dto.response.UserResponse;
import com.sakay.ecommerce.entity.Address;
import com.sakay.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getUsername()));
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<Address>> getAddresses(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getAddresses(userDetails.getUsername()));
    }

    @PostMapping("/addresses")
    public ResponseEntity<Address> addAddress(@AuthenticationPrincipal UserDetails userDetails,
                                              @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(userService.addAddress(userDetails.getUsername(), request));
    }

    @PutMapping("/addresses/{id}")
    public ResponseEntity<Address> updateAddress(@AuthenticationPrincipal UserDetails userDetails,
                                                 @PathVariable UUID id,
                                                 @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(userService.updateAddress(userDetails.getUsername(), id, request));
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<Void> deleteAddress(@AuthenticationPrincipal UserDetails userDetails,
                                              @PathVariable UUID id) {
        userService.deleteAddress(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/addresses/{id}/default")
    public ResponseEntity<Void> setDefault(@AuthenticationPrincipal UserDetails userDetails,
                                           @PathVariable UUID id) {
        userService.setDefaultAddress(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
