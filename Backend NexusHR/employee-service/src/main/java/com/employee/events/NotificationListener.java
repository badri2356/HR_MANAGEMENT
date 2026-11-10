package com.employee.events;

import com.employee.dtos.response.NotificationResponseDto;
import com.employee.services.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class NotificationListener {

    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;      // the "postman" that delivers WebSocket messages

    public NotificationListener(NotificationService notificationService,
                                SimpMessagingTemplate messagingTemplate) {
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleNotificationEvent(NotificationEvent event) {
        try {
            NotificationResponseDto saved = notificationService.create(
                    event.recipientEmail(), event.notificationType(), event.message());


            messagingTemplate.convertAndSendToUser(
                    event.recipientEmail().toLowerCase(),   // WHO: must match the email set in Part C
                    "/queue/notifications",                 // WHICH letterbox
                    saved);                                 // WHAT: the notification, sent as JSON

        } catch (Exception e) {
            log.error("Could not deliver {} notification to {}: {}",
                    event.notificationType(), event.recipientEmail(), e.getMessage());
        }
    }
}