package com.tourflow.platform.controller;

import com.tourflow.platform.dto.CreateNotificationRequest;
import com.tourflow.platform.dto.NotificationResponse;
import com.tourflow.platform.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody CreateNotificationRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notificationService.createNotification(request));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                notificationService.getMyNotifications(userId)
        );
    }

    @GetMapping("/mine/unread")
    public ResponseEntity<List<NotificationResponse>> getMyUnreadNotifications(
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                notificationService.getMyUnreadNotifications(userId)
        );
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                notificationService.markAsRead(id, userId)
        );
    }

    @PatchMapping("/mine/read-all")
    public ResponseEntity<Void> markAllAsRead(
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        notificationService.markAllAsRead(userId);

        return ResponseEntity.noContent().build();
    }
}
