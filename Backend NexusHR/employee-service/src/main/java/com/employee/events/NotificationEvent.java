package com.employee.events;

import com.employee.domain.enums.NotificationType;

public record NotificationEvent(
        String recipientEmail,
        NotificationType notificationType,
        String message
) {
}
