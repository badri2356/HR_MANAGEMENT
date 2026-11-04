package com.employee.events;

import com.employee.services.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class NotificationListener {
    private final NotificationService notificationService;

    public NotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
        log.info(">>> NotificationListener created");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleNotificationEvent(NotificationEvent event) {
        log.info(">>> Listener received: {} for {}", event.notificationType(), event.recipientEmail());
        try {
            notificationService.create(event.recipientEmail(), event.notificationType(), event.message());
        } catch (Exception e) {
            log.error(
                    "Could not deliver {} notification to {}:{}",
                    event.notificationType(),
                    event.recipientEmail(),
                    e.getMessage()
            );
        }
    }
}
