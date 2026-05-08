package com.project.orchestrate.modules.project_module.dto;

import com.project.orchestrate.modules.project_module.model.Project;
import com.project.orchestrate.modules.project_module.model.enums.ProjectStatus;
import com.project.orchestrate.modules.project_module.model.enums.ProjectType;
import com.project.orchestrate.modules.project_module.model.enums.ProjectVisibility;
import com.project.orchestrate.modules.user_module.dto.UserSummary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String slug,
        String description,
        String color,
        String coverImageUrl,
        ProjectType type,
        ProjectVisibility visibility,
        ProjectStatus status,
        LocalDate startDate,
        LocalDate targetDate,
        UUID organizationId,
        UserSummary createdBy,
        UserSummary lead,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getSlug(),
                project.getDescription(),
                project.getColor(),
                project.getCoverImageUrl(),
                project.getType(),
                project.getVisibility(),
                project.getStatus(),
                project.getStartDate(),
                project.getTargetDate(),
                project.getOrganization().getId(),
                UserSummary.from(project.getCreatedBy()),
                project.getLead() != null ? UserSummary.from(project.getLead()) : null,
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}

