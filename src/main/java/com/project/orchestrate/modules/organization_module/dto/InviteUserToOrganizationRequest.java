package com.project.orchestrate.modules.organization_module.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record InviteUserToOrganizationRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Role is required")
        @Pattern(
                regexp = "MEMBER|VIEWER",
                message = "Role must be one of: MEMBER, VIEWER"
        )
        String role
) {
}
