package com.zidio.nexushr.auth.services.service;

import com.zidio.nexushr.auth.dto.request.LoginRequest;
import com.zidio.nexushr.auth.dto.request.RefreshTokenRequest;
import com.zidio.nexushr.auth.dto.request.RegisterRequest;
import com.zidio.nexushr.auth.dto.response.AuthResponse;

public interface AuthService {


    public AuthResponse register(RegisterRequest registerRequest);

    public AuthResponse login(LoginRequest loginRequest);

    public AuthResponse refreshToken(RefreshTokenRequest request);

    public void logout(RefreshTokenRequest request);
}
