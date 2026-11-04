package com.zidio.nexushr.auth.domain.entity;

import com.zidio.nexushr.auth.domain.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor

public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    private String passwordHash;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<PasswordResetToken> passwordResetTokens = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<RefreshToken> refreshTokens = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    private Long employeeId;

    private Integer failedAttempts = 0;

    private Instant lastLoginAt;


}
