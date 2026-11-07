package com.zidio.nexushr.auth.controller;

import com.zidio.nexushr.auth.dto.request.*;
import com.zidio.nexushr.auth.dto.response.ApiResponse;
import com.zidio.nexushr.auth.messages.SuccessMessages;
import com.zidio.nexushr.auth.services.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        Boolean.FALSE,
                        authService.register(registerRequest),
                        SuccessMessages.USER_REGISTERED)
                );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(
                        Boolean.FALSE,
                        authService.login(loginRequest),
                        SuccessMessages.USER_LOGGED_IN)
                );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(
                        Boolean.FALSE,
                        authService.refreshToken(request),
                        SuccessMessages.REFRESHED_TOKEN)
                );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {

        authService.logout(refreshTokenRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(
                        Boolean.FALSE,
                        null,
                        SuccessMessages.USER_LOGGED_OUT)
                );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(
                Boolean.FALSE,
                null,
                SuccessMessages.LINK_SENT_TO_EMAIL)
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(
                Boolean.FALSE,
                null,
                SuccessMessages.PASSWORD_RESET_SUCCESS)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(
                Boolean.FALSE,
                authService.me(authentication.getName()),
                SuccessMessages.DATA_FETCHED_SUCCESSFULLY)
        );
    }
}
