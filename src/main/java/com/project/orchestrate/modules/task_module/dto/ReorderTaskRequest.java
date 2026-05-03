package com.project.orchestrate.modules.task_module.dto;

import com.project.orchestrate.modules.task_module.model.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for drag-and-drop reordering of tasks.
 * Used to move a task to a different status column and/or change its position.
 */
public record ReorderTaskRequest(
        @NotNull
        TaskStatus status,   // target column (may change status)

        @NotNull
        Double position      // new position value for ordering within the status column
) {}
