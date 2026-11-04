package com.zidio.nexushr.auth.domain.entity;

import com.zidio.nexushr.auth.domain.enums.AuditAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AuditAction action;

    @Column(nullable = false)
    private String performedBy;

    private String ipAddress;

    @Column(nullable = false)

    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

}
