package com.employee.config;

import com.employee.security.JwtAuthenticationFilter;
import com.employee.services.serviceImpl.JwtService;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity request, JwtService jwtService) {

        request
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/actuator/health", "/error").permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/departments",
                                "/api/departments/**"
                        ).permitAll()

                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .anyRequest().authenticated()

                )
                .exceptionHandling(e ->
                        e.authenticationEntryPoint((req, res, ex) ->
                                res.sendError(401, "Unauthorized"))
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class
                );
        return request.build();

    }
}
