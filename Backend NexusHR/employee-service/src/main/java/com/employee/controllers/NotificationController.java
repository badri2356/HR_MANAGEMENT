package com.employee.controllers;

import com.employee.dtos.response.ApiResponse;
import com.employee.messages.SuccessMessages;
import com.employee.services.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> myNotifications(@AuthenticationPrincipal String email) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, notificationService.getMyNotifications(email), SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }

    @GetMapping("/me/unread-count")
    public ResponseEntity<?> unreadCount(@AuthenticationPrincipal String email) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, notificationService.getUnreadCount(email), SuccessMessages.DATA_FETCHED_SUCCESSFULLY));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@AuthenticationPrincipal String email, Long notificationId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, notificationService.markAsRead(email, notificationId), SuccessMessages.DATA_UPDATED_SUCCESSFULLY));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@AuthenticationPrincipal String email) {
        notificationService.markAllAsRead(email);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(Boolean.FALSE, null, SuccessMessages.DATA_UPDATED_SUCCESSFULLY));
    }
}
