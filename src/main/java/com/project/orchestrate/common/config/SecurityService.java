package com.project.orchestrate.common.config;

import com.project.orchestrate.common.exception.AccessDeniedException;
import com.project.orchestrate.modules.auth_module.security.user.UserPrincipal;
import com.project.orchestrate.modules.organization_module.model.OrganizationMember;
import com.project.orchestrate.modules.organization_module.repository.OrganizationMemberRepository;
import com.project.orchestrate.modules.user_module.model.enums.MemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

@Service("securityService")
@RequiredArgsConstructor
public class SecurityService {

    private final OrganizationMemberRepository organizationMemberRepository;

    public boolean hasOrgRole(UUID organizationId, String... allowedRoles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            throw new AccessDeniedException("User is not authenticated");
        }

        OrganizationMember member = organizationMemberRepository
                .findByOrganizationIdAndUserId(organizationId, userPrincipal.getId())
                .orElseThrow(() -> new AccessDeniedException("User is not a member of the organization"));

        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new AccessDeniedException("User membership is not active");
        }

        boolean allowed = Arrays.stream(allowedRoles)
                .anyMatch(role -> role.equals(member.getRole().name()));
        if (!allowed) {
            throw new AccessDeniedException("User does not have the required role");
        }
        return true;
    }
}
