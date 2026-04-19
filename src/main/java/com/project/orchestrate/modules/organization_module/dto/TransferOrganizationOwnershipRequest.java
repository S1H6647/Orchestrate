package com.project.orchestrate.modules.organization_module.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TransferOrganizationOwnershipRequest(
        @NotNull(message = "New owner user ID is required")
        UUID newOwnerUserId
) {
}
