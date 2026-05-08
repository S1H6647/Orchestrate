package com.project.orchestrate.modules.notification_module.dto;

import com.project.orchestrate.modules.notification_module.model.Notification;
import com.project.orchestrate.modules.notification_module.model.enums.TaskType;

public record NotificationResponse(
        TaskType type,
        String content
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getType(),
                notification.getContent()
        );
    }
}
