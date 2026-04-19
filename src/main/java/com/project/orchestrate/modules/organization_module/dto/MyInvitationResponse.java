package com.project.orchestrate.modules.organization_module.dto;

import com.project.orchestrate.modules.organization_module.model.OrganizationMember;
import com.project.orchestrate.modules.user_module.model.enums.MemberStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record MyInvitationResponse(
        UUID organizationId,
        String organizationName,
        String organizationSlug,
        String role,
        String token,
        LocalDateTime expiresAt,
        MemberStatus status
) {
    public static MyInvitationResponse from(OrganizationMember member) {
        return new MyInvitationResponse(
                member.getOrganization().getId(),
                member.getOrganization().getName(),
                member.getOrganization().getSlug(),
                member.getRole().name(),
                member.getInviteToken(),
                member.getInviteExpiresAt(),
                member.getStatus()
        );
    }
}
