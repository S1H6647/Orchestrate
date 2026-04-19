package com.project.orchestrate.modules.organization_module.dto;

import com.project.orchestrate.modules.organization_module.model.OrganizationMember;
import com.project.orchestrate.modules.organization_module.model.enums.OrganizationRole;
import com.project.orchestrate.modules.organization_module.model.enums.OrganizationStatus;
import com.project.orchestrate.modules.organization_module.model.enums.Plan;
import com.project.orchestrate.modules.user_module.model.enums.MemberStatus;

import java.util.UUID;

public record CurrentUserOrganizationResponse(
        UUID id,
        String name,
        String slug,
        String logoUrl,
        Plan plan,
        OrganizationStatus status,
        OrganizationRole myRole,
        MemberStatus membershipStatus
) {
    public static CurrentUserOrganizationResponse from(OrganizationMember member) {
        return new CurrentUserOrganizationResponse(
                member.getOrganization().getId(),
                member.getOrganization().getName(),
                member.getOrganization().getSlug(),
                member.getOrganization().getLogoUrl(),
                member.getOrganization().getPlan(),
                member.getOrganization().getStatus(),
                member.getRole(),
                member.getStatus()
        );
    }
}
