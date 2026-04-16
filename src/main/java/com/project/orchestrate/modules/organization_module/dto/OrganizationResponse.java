package com.project.orchestrate.modules.project_module.dto;

import com.project.orchestrate.modules.project_module.model.enums.Plan;
import com.project.orchestrate.modules.user_module.dto.UserSummary;
import com.project.orchestrate.modules.user_module.model.enums.OrganizationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        String name,
        String slug,
        String description,
        String logoUrl,
        String websiteUrl,
        Plan plan,
        int maxMembers,
        int maxProjects,
        OrganizationStatus status,
        UserSummary createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

