package com.project.orchestrate.modules.notification_module.service;

import com.project.orchestrate.common.exception.ResourceNotFoundException;
import com.project.orchestrate.modules.notification_module.dto.NotificationResponse;
import com.project.orchestrate.modules.notification_module.model.Notification;
import com.project.orchestrate.modules.notification_module.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Page<NotificationResponse> getAllNotifications(UUID userId, Pageable pageable, boolean unread) {
        Page<Notification> notifications;

        if (unread) {
            notifications = notificationRepository.findAllByRecipientIdAndIsReadFalse(userId, pageable);
        } else {
            notifications = notificationRepository.findAllByRecipientId(userId, pageable);
        }

        return notifications.map(NotificationResponse::from);
    }

    public void markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadByRecipientId(userId);
    }
}
