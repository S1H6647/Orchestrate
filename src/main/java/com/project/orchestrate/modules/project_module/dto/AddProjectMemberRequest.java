package com.project.orchestrate.modules.project_module.dto;

import com.project.orchestrate.modules.project_module.model.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddProjectMemberRequest(
        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Role is required")
        ProjectRole role
) {
}

