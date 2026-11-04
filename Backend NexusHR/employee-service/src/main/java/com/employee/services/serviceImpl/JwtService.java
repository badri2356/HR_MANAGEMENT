package com.employee.services.serviceImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.List;

@Service
public class JwtService {
    private final SecretKey secretKey;

    public JwtService(@Value("${jwt.secret}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public List<String> getRoles(Claims claims) {
        List<?> roles = claims.get("roles", List.class);
        return roles == null ? List.of() : roles.stream().map(String::valueOf).toList();
    }
}
