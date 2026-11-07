package com.zidio.nexushr.auth.services.service;

import com.zidio.nexushr.auth.dto.request.*;
import com.zidio.nexushr.auth.dto.response.AuthResponse;
import com.zidio.nexushr.auth.dto.response.MeResponseDto;

public interface AuthService {


    public AuthResponse register(RegisterRequest registerRequest);

    public AuthResponse login(LoginRequest loginRequest);

    public AuthResponse refreshToken(RefreshTokenRequest request);

    public void logout(RefreshTokenRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    public MeResponseDto me(String email);
}
