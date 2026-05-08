package com.project.orchestrate.modules.notification_module.dto;

import com.project.orchestrate.modules.task_module.dto.TaskResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskCreatedEvent(
        String eventType,
        UUID projectId,
        TaskResponse task,
        UUID userId,
        String name,
        LocalDateTime timestamp
) {
}
