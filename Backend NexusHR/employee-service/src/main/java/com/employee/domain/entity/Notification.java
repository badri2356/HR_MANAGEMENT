package com.employee.domain.entity;

import com.employee.domain.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification extends BaseEntity {

    @Column(nullable = false)
    private String recipientEmail;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private boolean isRead = false;

}
