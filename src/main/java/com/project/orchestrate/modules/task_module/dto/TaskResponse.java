package com.project.orchestrate.modules.task_module.dto;

import com.project.orchestrate.modules.task_module.model.Task;
import com.project.orchestrate.modules.task_module.model.enums.TaskPriority;
import com.project.orchestrate.modules.task_module.model.enums.TaskStatus;
import com.project.orchestrate.modules.user_module.dto.UserSummary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String identifier,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Double position,
        LocalDate dueDate,
        LocalDateTime completedAt,
        Integer storyPoints,
        UUID projectId,
        UserSummary assignee,
        UserSummary reporter,
        UUID parentTaskId,
        List<LabelResponse> labels,
        int subTaskCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TaskResponse from(Task task) {
        if (task == null) {
            return null;
        }

        return new TaskResponse(
                task.getId(),
                task.getIdentifier(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getPosition(),
                task.getDueDate(),
                task.getCompletedAt(),
                task.getStoryPoints(),
                task.getProject() != null ? task.getProject().getId() : null,
                task.getAssignee() != null ? new UserSummary(
                        task.getAssignee().getId(),
                        task.getAssignee().getName(),
                        task.getAssignee().getEmail()
                ) : null,
                task.getReporter() != null ? new UserSummary(
                        task.getReporter().getId(),
                        task.getReporter().getName(),
                        task.getReporter().getEmail()
                ) : null,
                task.getParentTask() != null ? task.getParentTask().getId() : null,
                task.getLabels() != null ? task.getLabels().stream()
                        .map(LabelResponse::from)
                        .toList() : List.of(),
                task.getSubTasks() != null ? task.getSubTasks().size() : 0,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
