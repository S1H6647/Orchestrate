package com.project.orchestrate.modules.project_module.dto;

import com.project.orchestrate.modules.project_module.model.ProjectMember;
import com.project.orchestrate.modules.project_module.model.enums.ProjectRole;
import com.project.orchestrate.modules.user_module.dto.UserSummary;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectMemberResponse(
        UUID id,
        UserSummary user,
        ProjectRole role,
        LocalDateTime joinedAt
) {
    public static ProjectMemberResponse from(ProjectMember member) {
        return new ProjectMemberResponse(
                member.getId(),
                UserSummary.from(member.getUser()),
                member.getRole(),
                member.getJoinedAt()
        );
    }
}

