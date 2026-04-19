package com.project.orchestrate.modules.organization_module.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ConfirmDeleteOrganizationRequest(
        @NotBlank(message = "Confirmation message is required")
        @Pattern(
                regexp = "^DELETE/[a-z0-9]+(?:-[a-z0-9]+)*$",
                message = "Confirmation must be in format DELETE/<organization-slug>"
        )
        String confirmation
) {
}
