package com.sakay.ecommerce.service;

import com.sakay.ecommerce.dto.request.LoginRequest;
import com.sakay.ecommerce.dto.request.RegisterRequest;
import com.sakay.ecommerce.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse registerAdmin(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    void logout(String refreshToken);
}