package com.project.orchestrate.modules.notification_module.dto;

import com.project.orchestrate.modules.task_module.model.enums.TaskStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record TaskMoveEvent(
        String eventType,
        UUID taskId,
        UUID projectId,
        TaskStatus fromStatus,
        TaskStatus toStatus,
        Double position,
        UUID userId,
        String name,
        LocalDateTime timestamp) {
}
