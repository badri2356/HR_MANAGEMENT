package com.zidio.nexushr.auth.services.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

public interface JwtService {

    public String generateAccessToken(UserDetails userDetails);

    public String extractUsername(String token);

    public Date extractExpiration(String token);

    public boolean isTokenExpired(String token);

    public boolean isTokenValid(String token, UserDetails userDetails);

}
