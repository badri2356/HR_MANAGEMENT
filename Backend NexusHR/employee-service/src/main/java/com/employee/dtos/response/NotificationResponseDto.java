package com.employee.dtos.response;

import com.employee.domain.enums.NotificationType;

import java.time.Instant;

public record NotificationResponseDto(
        Long id,
        NotificationType type,
        String title,
        String message,
        boolean read,
        Instant createdAt
) {
}
