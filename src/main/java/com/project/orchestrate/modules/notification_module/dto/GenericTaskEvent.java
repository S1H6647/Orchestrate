package com.project.orchestrate.modules.notification_module.dto;

import com.project.orchestrate.modules.notification_module.model.enums.TaskType;
import com.project.orchestrate.modules.task_module.model.enums.TaskStatus;
import jakarta.annotation.Nullable;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.UUID;

public record GenericTaskEvent(
        TaskType eventType,
        UUID taskId,
        UUID projectId,
        @Nullable TaskStatus fromStatus,
        @Nullable TaskStatus toStatus,
        @Nullable Double position,
        UUID userId,
        String name,
        String taskTitle,
        @Length(max = 50) String content,
        LocalDateTime timestamp
) {
}
