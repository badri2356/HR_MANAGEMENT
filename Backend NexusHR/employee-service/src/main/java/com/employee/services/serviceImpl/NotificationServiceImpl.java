package com.employee.services.serviceImpl;

import com.employee.domain.entity.Notification;
import com.employee.domain.enums.NotificationType;
import com.employee.dtos.response.NotificationResponseDto;
import com.employee.exceptions.ResourceNotFoundException;
import com.employee.repositories.NotificationRepository;
import com.employee.services.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    @Override
    public NotificationResponseDto create(String email, NotificationType notificationType, String message) {
        Notification notification = new Notification();
        notification.setRecipientEmail(email.toLowerCase());
        notification.setNotificationType(notificationType);
        notification.setMessage(message);
        notification.setTitle(notificationType.getTitle());
        Notification save = notificationRepository.save(notification);

        return toResponse(save);
    }

    @Override
    public List<NotificationResponseDto> getMyNotifications(String email) {

        return notificationRepository.findByRecipientEmail(email.toLowerCase())
                .stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Override
    public long getUnreadCount(String email) {

        long count = notificationRepository.findByRecipientEmail(email.toLowerCase())
                .stream()
                .filter(notification -> !notification.isRead())
                .count();
        return count;
    }

    @Override
    public NotificationResponseDto markAsRead(String email, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .filter(n -> n.getRecipientEmail().equalsIgnoreCase(email))
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        notification.setRead(true);
        notificationRepository.save(notification);
        return toResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public void markAllAsRead(String email) {
        notificationRepository.findByRecipientEmail(email.toLowerCase())
                .forEach(notification -> {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                });

    }

    private NotificationResponseDto toResponse(Notification save) {
        return new NotificationResponseDto(
                save.getId(),
                save.getNotificationType(),
                save.getTitle(),
                save.getMessage(),
                save.isRead(),
                save.getCreatedAt()
        );
    }
}
