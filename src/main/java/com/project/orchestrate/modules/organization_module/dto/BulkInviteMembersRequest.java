package com.project.orchestrate.modules.organization_module.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record BulkInviteMembersRequest(
        @NotEmpty(message = "Invites list is required")
        List<@Valid InviteMember> invites
) {
    public record InviteMember(
            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            String email,

            @NotBlank(message = "Role is required")
            @Pattern(
                    regexp = "MEMBER|VIEWER",
                    message = "Role must be one of: MEMBER or VIEWER"
            )
            String role
    ) {
    }
}
