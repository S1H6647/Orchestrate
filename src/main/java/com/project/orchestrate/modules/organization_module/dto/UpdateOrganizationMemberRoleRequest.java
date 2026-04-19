package com.project.orchestrate.modules.organization_module.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateOrganizationMemberRoleRequest(
        @NotBlank(message = "Role is required")
        @Pattern(
                regexp = "OWNER|ADMIN|MEMBER|VIEWER",
                message = "Role must be one of: OWNER, ADMIN, MEMBER, VIEWER"
        )
        String role
) {
}
