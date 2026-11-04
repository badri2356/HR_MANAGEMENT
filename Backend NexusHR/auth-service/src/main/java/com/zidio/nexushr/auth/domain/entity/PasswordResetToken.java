package com.zidio.nexushr.auth.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "password_reset_token")
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetToken extends BaseEntity {
    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

}
