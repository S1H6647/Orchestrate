package com.project.orchestrate.modules.organization_module.dto;

import com.project.orchestrate.modules.organization_module.model.OrganizationMember;
import com.project.orchestrate.modules.organization_module.model.enums.OrganizationRole;
import com.project.orchestrate.modules.user_module.dto.UserSummary;
import com.project.orchestrate.modules.user_module.model.User;
import com.project.orchestrate.modules.user_module.model.enums.MemberStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record MemberAddedToOrganizationResponse(
        UUID memberId,
        UserSummary user,
        OrganizationRole role,
        MemberStatus status,
        LocalDateTime joinedAt
) {

    public static MemberAddedToOrganizationResponse from(OrganizationMember organizationMember) {
        User user = organizationMember.getUser();

        return new MemberAddedToOrganizationResponse(
                organizationMember.getId(),
                new UserSummary(user.getId(), user.getName(), user.getEmail()),
                organizationMember.getRole(),
                organizationMember.getStatus(),
                organizationMember.getJoinedAt()
        );
    }
}
