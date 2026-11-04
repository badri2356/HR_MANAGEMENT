package com.zidio.nexushr.auth.services.serviceImpl;

import com.zidio.nexushr.auth.services.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtServiceImpl implements JwtService {



    private final SecretKey secretKey;
    private final long accessTokenExpiration;

    public JwtServiceImpl(@Value("${jwt.secret}") String secretKey,
                          @Value("${jwt.access-token-expiration}") long accessTokenExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        this.accessTokenExpiration = accessTokenExpiration;
    }

    @Override
    public String generateAccessToken(UserDetails userDetails) {

        Instant now = Instant.now();

        Instant expiry = now.plusMillis(accessTokenExpiration);

        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    @Override
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    @Override
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);


            return username.equals(userDetails.getUsername())
                    && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }

    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
