package com.employee.services.service;

import com.employee.domain.enums.NotificationType;
import com.employee.dtos.response.NotificationResponseDto;

import java.util.List;

public interface NotificationService {
    NotificationResponseDto create(String Email,
                                   NotificationType notificationType,
                                   String message
    );

    List<NotificationResponseDto> getMyNotifications(String email);

    long getUnreadCount(String email);

    NotificationResponseDto markAsRead(String email, Long notificationId);

    void markAllAsRead(String email);
}
