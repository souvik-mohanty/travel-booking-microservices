package com.tourflow.platform.service;

import com.tourflow.platform.domain.Notification;
import com.tourflow.platform.dto.CreateNotificationRequest;
import com.tourflow.platform.dto.NotificationResponse;
import com.tourflow.platform.exception.NotificationAccessDeniedException;
import com.tourflow.platform.exception.NotificationNotFoundException;
import com.tourflow.platform.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {

        Notification notification = new Notification(
                UUID.randomUUID(),
                request.userId(),
                request.title(),
                request.message(),
                false,
                OffsetDateTime.now()
        );

        Notification saved = notificationRepository.save(notification);

        return NotificationResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(UUID userId) {

        return notificationRepository.findByUserId(userId)
                .stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyUnreadNotifications(UUID userId) {

        return notificationRepository.findByUserIdAndReadFalse(userId)
                .stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, UUID userId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new NotificationAccessDeniedException(
                    "You cannot manage another user's notification"
            );
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notification = notificationRepository.save(notification);
        }

        return NotificationResponse.fromEntity(notification);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {

        List<Notification> unread = notificationRepository.findByUserIdAndReadFalse(userId);

        unread.forEach(notification -> notification.setRead(true));

        notificationRepository.saveAll(unread);
    }
}
