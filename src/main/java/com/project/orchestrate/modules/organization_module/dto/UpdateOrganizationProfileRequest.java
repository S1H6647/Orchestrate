package com.project.orchestrate.modules.organization_module.dto;

import jakarta.validation.constraints.Size;

public record UpdateOrganizationProfileRequest(
        @Size(max = 500, message = "Description must be at most 500 characters")
        String description,

        @Size(max = 255, message = "Website URL must be at most 255 characters")
        String websiteUrl
) {
}
