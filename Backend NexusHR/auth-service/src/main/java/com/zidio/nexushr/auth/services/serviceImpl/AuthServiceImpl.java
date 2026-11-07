package com.zidio.nexushr.auth.services.serviceImpl;

import com.zidio.nexushr.auth.domain.entity.PasswordResetToken;
import com.zidio.nexushr.auth.domain.entity.RefreshToken;
import com.zidio.nexushr.auth.domain.entity.Role;
import com.zidio.nexushr.auth.domain.entity.User;
import com.zidio.nexushr.auth.domain.enums.RoleName;
import com.zidio.nexushr.auth.domain.enums.UserStatus;
import com.zidio.nexushr.auth.dto.request.*;
import com.zidio.nexushr.auth.dto.response.AuthResponse;
import com.zidio.nexushr.auth.dto.response.MeResponseDto;
import com.zidio.nexushr.auth.exceptions.AccountInactiveException;
import com.zidio.nexushr.auth.exceptions.ResourceNotFoundException;
import com.zidio.nexushr.auth.exceptions.UserAlreadyExistsException;
import com.zidio.nexushr.auth.repositories.PasswordResetTokenRepository;
import com.zidio.nexushr.auth.repositories.RefreshTokenRepository;
import com.zidio.nexushr.auth.repositories.RoleRepository;
import com.zidio.nexushr.auth.repositories.UserRepository;
import com.zidio.nexushr.auth.security.CustomUserDetails;
import com.zidio.nexushr.auth.security.TokenUtil;
import com.zidio.nexushr.auth.services.service.AuthService;
import com.zidio.nexushr.auth.services.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private static final long RESET_TOKEN_MINUTES = 15;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // Where the reset link points (the frontend's reset page)
    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           JwtService jwtService,
                           PasswordEncoder passwordEncoder,
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordResetTokenRepository passwordResetTokenRepository,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
    }


    @Override
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        String email = registerRequest.getEmail().trim().toLowerCase();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("User already exists with email: " + email);
        }

        Role employeeRole = roleRepository.findByName(RoleName.EMPLOYEE)
                .orElseThrow(() -> new ResourceNotFoundException("EMPLOYEE role not found"));

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRoles(Set.of(employeeRole));
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedAttempts(0);
        User savedUser = userRepository.save(user);

        // 5. Log them straight in
        return issueTokens(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {

        String email = loginRequest.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountInactiveException("User is not active");
        }

        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(TokenUtil.sha256Hex(request.getRefreshToken()))
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BadCredentialsException("Refresh token has expired");
        }

        User user = refreshToken.getUser();
        if (user.getStatus() != UserStatus.ACTIVE) {
            refreshTokenRepository.delete(refreshToken);
            throw new AccountInactiveException("User is not active");
        }

        String accessToken = jwtService.generateAccessToken(new CustomUserDetails(user));

        return new AuthResponse(accessToken, request.getRefreshToken(), "Bearer", accessTokenExpiration);
    }


    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(TokenUtil.sha256Hex(request.getRefreshToken()))
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        refreshTokenRepository.delete(refreshToken);
    }


    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        userRepository.findByEmail(email).ifPresent(user -> {

            // Only the newest link should work
            passwordResetTokenRepository.deleteByUser(user);

            // The raw token goes in the link. only its hash is stored
            String rawToken = TokenUtil.newToken();

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setToken(TokenUtil.sha256Hex(rawToken));
            resetToken.setExpiresAt(Instant.now().plus(RESET_TOKEN_MINUTES, ChronoUnit.MINUTES));
            passwordResetTokenRepository.save(resetToken);

            String link = frontendUrl + " /reset-password?token=" + rawToken;
            emailService.sendPasswordReset(user.getEmail(), link);
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        // Look the token up by its hash
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(TokenUtil.sha256Hex(request.getToken()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link"));

        // Expired? Delete it and refuse, with the same message (no hint whether it existed)
        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Invalid or expired reset link");
        }

        // Save the new password, hashed with Argon2
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // The link works only once
        passwordResetTokenRepository.delete(resetToken);

        // Log out everywhere, in case someone else knew the old password
        refreshTokenRepository.deleteByUser(user);
    }

    @Override
    public MeResponseDto me(String email) {

        User user = userRepository.findWithRolesByEmail(email)
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with email: " + email)
                );
        Set<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toCollection(TreeSet::new));

        return new MeResponseDto(
                user.getId(),
                user.getEmail(),
                user.getStatus().name(),
                roles,
                user.getEmployeeId()
        );
    }


    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(new CustomUserDetails(user));

        String rawRefreshToken = TokenUtil.newToken();

        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setToken(TokenUtil.sha256Hex(rawRefreshToken));
        refreshTokenEntity.setExpiresAt(Instant.now().plusMillis(refreshTokenExpiration));
        refreshTokenRepository.save(refreshTokenEntity);

        return new AuthResponse(accessToken, rawRefreshToken, "Bearer", accessTokenExpiration);
    }
}