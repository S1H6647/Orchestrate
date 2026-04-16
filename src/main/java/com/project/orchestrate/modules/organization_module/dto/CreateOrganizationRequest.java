package com.project.orchestrate.modules.organization_module.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(
        @NotBlank(message = "Organization name is required")
        @Size(max = 100, message = "Organization name must be at most 100 characters")
        String name,

        @Size(max = 100, message = "Organization slug must be at most 100 characters")
        @Pattern(
                regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                message = "Slug must be lowercase letters, numbers, and hyphens only"
        )
        String slug,

        @Size(max = 500, message = "Description must be at most 500 characters")
        String description,

        @Size(max = 255, message = "Logo URL must be at most 255 characters")
        String logoUrl,

        @Size(max = 255, message = "Website URL must be at most 255 characters")
        String websiteUrl
) {
}

