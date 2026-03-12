package com.sakay.ecommerce.service.impl;

import com.sakay.ecommerce.dto.request.LoginRequest;
import com.sakay.ecommerce.dto.request.RegisterRequest;
import com.sakay.ecommerce.dto.response.AuthResponse;
import com.sakay.ecommerce.dto.response.UserResponse;
import com.sakay.ecommerce.entity.RefreshToken;
import com.sakay.ecommerce.entity.User;
import com.sakay.ecommerce.exception.BadRequestException;
import com.sakay.ecommerce.exception.UnauthorizedException;
import com.sakay.ecommerce.repository.RefreshTokenRepository;
import com.sakay.ecommerce.repository.UserRepository;
import com.sakay.ecommerce.security.JwtService;
import com.sakay.ecommerce.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(User.Role.CUSTOMER)
                .build();

        userRepository.save(user);
        return buildAuthResponse(user, null);
    }

    @Override
    @Transactional
    public AuthResponse registerAdmin(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(User.Role.ADMIN)
                .build();

        userRepository.save(user);
        return buildAuthResponse(user, null);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        String refreshToken = createRefreshToken(user, request.getDeviceInfo());
        return buildAuthResponse(user, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (token.getIsRevoked() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token expired or revoked");
        }

        token.setIsRevoked(true);
        refreshTokenRepository.save(token);

        String newRefresh = createRefreshToken(token.getUser(), null);
        return buildAuthResponse(token.getUser(), newRefresh);
    }

    @Override
    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue).ifPresent(rt -> {
            rt.setIsRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    private String createRefreshToken(User user, String deviceInfo) {
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .deviceInfo(deviceInfo)
                .build();
        refreshTokenRepository.save(token);
        return tokenValue;
    }

    private AuthResponse buildAuthResponse(User user, String refreshToken) {
        String accessToken = jwtService.generateToken(user.getEmail(),
                Map.of("role", user.getRole().name()));
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(UserResponse.from(user))
                .build();
    }
}