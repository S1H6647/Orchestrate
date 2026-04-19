package com.project.orchestrate.modules.organization_module.dto;

import com.project.orchestrate.modules.organization_module.model.OrganizationMember;
import com.project.orchestrate.modules.user_module.model.enums.MemberStatus;

import java.time.LocalDateTime;

public record OrganizationInvitationResponse(
        String email,
        String role,
        String token,
        LocalDateTime expiresAt,
        MemberStatus status
) {
    public static OrganizationInvitationResponse from(OrganizationMember member) {
        return new OrganizationInvitationResponse(
                member.getUser().getEmail(),
                member.getRole().name(),
                member.getInviteToken(),
                member.getInviteExpiresAt(),
                member.getStatus()
        );
    }
}
