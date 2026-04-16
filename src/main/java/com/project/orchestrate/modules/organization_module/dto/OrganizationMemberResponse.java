package com.project.orchestrate.modules.organization_module.dto;

import com.project.orchestrate.modules.organization_module.model.OrganizationMember;
import com.project.orchestrate.modules.organization_module.model.enums.OrganizationRole;
import com.project.orchestrate.modules.user_module.dto.UserSummary;
import com.project.orchestrate.modules.user_module.model.User;
import com.project.orchestrate.modules.user_module.model.enums.MemberStatus;

public record OrganizationMemberResponse(
        UserSummary user,
        MemberStatus status,
        OrganizationRole role
) {

    public static OrganizationMemberResponse from(OrganizationMember organizationMember) {
        User user = organizationMember.getUser();

        return new OrganizationMemberResponse(
                new UserSummary(user.getId(), user.getName(), user.getEmail()),
                organizationMember.getStatus(),
                organizationMember.getRole()
        );
    }
}
