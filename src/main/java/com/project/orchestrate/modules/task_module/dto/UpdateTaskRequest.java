package com.project.orchestrate.modules.task_module.dto;

import com.project.orchestrate.modules.task_module.model.enums.TaskPriority;
import com.project.orchestrate.modules.task_module.model.enums.TaskStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UpdateTaskRequest(
        @Size(max = 255)
        String title,

        String description,

        TaskStatus status,

        TaskPriority priority,

        LocalDate dueDate,

        Integer storyPoints,

        UUID assigneeId,

        List<UUID> labelIds
) {}
