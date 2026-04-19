package com.project.orchestrate.modules.organization_module.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateOrganizationIdentityRequest(
        @Size(max = 100, message = "Organization name must be at most 100 characters")
        String name,

        @Size(max = 100, message = "Organization slug must be at most 100 characters")
        @Pattern(
                regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                message = "Slug must be lowercase letters, numbers, and hyphens only"
        )
        String slug
) {
}
