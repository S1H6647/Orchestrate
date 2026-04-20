package com.project.orchestrate.modules.organization_module.dto;

import java.util.UUID;

public record OrganizationUsageMetricsResponse(
        UUID organizationId,
        int totalMembers,
        int maxMembers,
        int remainingMembers,
        int totalProjects,
        int maxProjects,
        int remainingProjects
) {
}
