package com.project.orchestrate.modules.user_module.dto;

import com.project.orchestrate.modules.organization_module.model.enums.OrganizationStatus;
import com.project.orchestrate.modules.organization_module.model.enums.Plan;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrganizationSummary(
        UUID id,
        String name,
        String slug,
        Plan plan,
        int maxMembers,
        int maxProjects,
        OrganizationStatus status,
        LocalDateTime createdAt
) {
}
