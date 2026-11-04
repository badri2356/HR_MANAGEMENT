package com.employee.security;

import com.employee.services.serviceImpl.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            Claims claims = jwtService.parse(header.substring(7));
            List<SimpleGrantedAuthority> authorities = jwtService.getRoles(claims)
                    .stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        } catch (JwtException | IllegalArgumentException ex) {
            System.out.println("Jwt rejected: " + ex.getMessage());
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

}
