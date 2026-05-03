package com.project.orchestrate.modules.task_module.dto;

import com.project.orchestrate.modules.task_module.model.enums.TaskPriority;
import com.project.orchestrate.modules.task_module.model.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateTaskRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title cannot exceed 255 characters")
        String title,

        String description,

        TaskStatus status,

        TaskPriority priority,

        LocalDate dueDate,

        Integer storyPoints,

        UUID assigneeId,      // optional

        UUID parentTaskId,    // optional — creates sub-task

        List<UUID> labelIds   // optional
) {
}
