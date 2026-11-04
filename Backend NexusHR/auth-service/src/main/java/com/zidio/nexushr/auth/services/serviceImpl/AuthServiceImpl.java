package com.zidio.nexushr.auth.services.serviceImpl;

import com.zidio.nexushr.auth.domain.entity.RefreshToken;
import com.zidio.nexushr.auth.domain.entity.Role;
import com.zidio.nexushr.auth.domain.entity.User;
import com.zidio.nexushr.auth.domain.enums.RoleName;
import com.zidio.nexushr.auth.domain.enums.UserStatus;
import com.zidio.nexushr.auth.dto.request.LoginRequest;
import com.zidio.nexushr.auth.dto.request.RefreshTokenRequest;
import com.zidio.nexushr.auth.dto.request.RegisterRequest;
import com.zidio.nexushr.auth.dto.response.AuthResponse;
import com.zidio.nexushr.auth.exceptions.AccountInactiveException;
import com.zidio.nexushr.auth.exceptions.ResourceNotFoundException;
import com.zidio.nexushr.auth.exceptions.UserAlreadyExistsException;
import com.zidio.nexushr.auth.repositories.RefreshTokenRepository;
import com.zidio.nexushr.auth.repositories.RoleRepository;
import com.zidio.nexushr.auth.repositories.UserRepository;
import com.zidio.nexushr.auth.security.CustomUserDetails;
import com.zidio.nexushr.auth.services.service.AuthService;
import com.zidio.nexushr.auth.services.service.JwtService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository, JwtService jwtService, PasswordEncoder passwordEncoder, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
    }


    @Override
    public AuthResponse register(RegisterRequest registerRequest) {

        //Check email already exists
        User existingUser = userRepository.findByEmail(registerRequest.getEmail())
                .orElse(null);

        if (existingUser != null) {
            throw new UserAlreadyExistsException(
                    "User already exists with email: " + registerRequest.getEmail()
            );
        }

        //Find default employee role
        Role employeeRole = roleRepository.findByName(RoleName.EMPLOYEE)
                .orElseThrow(() -> new ResourceNotFoundException("EMPLOYEE role not found"));

        //Create new User
        User user = new User();

        //Set user details
        user.setEmail(registerRequest.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRoles(Set.of(employeeRole));
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedAttempts(0);

        //Save user
        User savedUser = userRepository.save(user);

        //Generate access token
        String accessToken = jwtService.generateAccessToken(
                new CustomUserDetails(savedUser)
        );

        //Generate refresh token
        String refreshToken = refreshToken();

        RefreshToken token = new RefreshToken();

        //Save refresh token to database
        token.setUser(savedUser);
        token.setToken(refreshToken);
        token.setExpiresAt(Instant.now().plusMillis(refreshTokenExpiration));

        refreshTokenRepository.save(token);

        //Return response
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                accessTokenExpiration
        );
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {

        String email = loginRequest.getEmail().trim().toLowerCase();

        //check if user exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        //check password matches or not
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        //user validation
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountInactiveException("User is not active");
        }

        //generate access token
        String token = jwtService.generateAccessToken(new CustomUserDetails(user));

        //generate refresh token
        String refreshToken = refreshToken();

        //save refresh token in database
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setToken(refreshToken);
        refreshTokenEntity.setExpiresAt(Instant.now().plusMillis(refreshTokenExpiration));

        refreshTokenRepository.save(refreshTokenEntity);

        //return response
        return new AuthResponse(
                token,
                refreshToken,
                "Bearer",
                accessTokenExpiration
        );
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token has expired");
        }

        User user = refreshToken.getUser();
        if (user.getStatus() != UserStatus.ACTIVE) {
            refreshTokenRepository.delete(refreshToken);
            throw new AccountInactiveException("User is not active");
        }


        String accessToken = jwtService.generateAccessToken(
                new CustomUserDetails(user)
        );


        return new AuthResponse(
                accessToken,
                request.getRefreshToken(),
                "Bearer",
                accessTokenExpiration
        );
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        refreshTokenRepository.delete(refreshToken);

    }

    private String refreshToken() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }
}
