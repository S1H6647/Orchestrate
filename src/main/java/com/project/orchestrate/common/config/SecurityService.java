package com.project.orchestrate.common.config;

import com.project.orchestrate.common.exception.AccessDeniedException;
import com.project.orchestrate.modules.auth_module.security.user.UserPrincipal;
import com.project.orchestrate.modules.organization_module.model.OrganizationMember;
import com.project.orchestrate.modules.organization_module.repository.OrganizationMemberRepository;
import com.project.orchestrate.modules.project_module.model.Project;
import com.project.orchestrate.modules.project_module.model.ProjectMember;
import com.project.orchestrate.modules.project_module.model.enums.ProjectVisibility;
import com.project.orchestrate.modules.project_module.repository.ProjectMemberRepository;
import com.project.orchestrate.modules.project_module.repository.ProjectRepository;
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
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;

    public boolean hasOrgRole(UUID organizationId, String... allowedRoles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            throw new AccessDeniedException("User is not authenticated");
        }

        if ("SYSTEM_ADMIN".equals(userPrincipal.getRole())) {
            return true;
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

    public boolean hasProjectRole(UUID projectId, String... allowedRoles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            throw new AccessDeniedException("User is not authenticated");
        }

        if ("SYSTEM_ADMIN".equals(userPrincipal.getRole())) {
            return true;
        }

        ProjectMember member = projectMemberRepository
                .findByProjectIdAndUserId(projectId, userPrincipal.getId())
                .orElseThrow(() -> new AccessDeniedException("User is not a member of the project"));

        boolean allowed = Arrays.stream(allowedRoles)
                .anyMatch(role -> role.equals(member.getRole().name()));
        if (!allowed) {
            throw new AccessDeniedException("User does not have the required role");
        }
        return true;
    }

    public boolean hasProjectAccess(UUID projectId, String... allowedRoles) {
        return hasProjectRole(projectId, allowedRoles);
    }

    public boolean canAccessProjectBySlug(UUID orgId, String projectSlug) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            throw new AccessDeniedException("User is not authenticated");
        }

        if ("SYSTEM_ADMIN".equals(userPrincipal.getRole())) {
            return true;
        }

        Project project = projectRepository.findByOrganizationIdAndSlug(orgId, projectSlug)
                .orElseThrow(() -> new AccessDeniedException("Project not found"));

        OrganizationMember orgMember = organizationMemberRepository
                .findByOrganizationIdAndUserId(orgId, userPrincipal.getId())
                .orElseThrow(() -> new AccessDeniedException("User is not a member of the organization"));

        if (orgMember.getStatus() != MemberStatus.ACTIVE) {
            throw new AccessDeniedException("User membership is not active");
        }

        String orgRole = orgMember.getRole().name();
        if ("OWNER".equals(orgRole) || "ADMIN".equals(orgRole)) {
            return true;
        }

        if (project.getVisibility() == ProjectVisibility.PUBLIC) {
            return true;
        }

        if (projectMemberRepository.existsByProjectIdAndUserId(project.getId(), userPrincipal.getId())) {
            return true;
        }

        throw new AccessDeniedException("User does not have access to this project");
    }

    public boolean hasProjectRoleOrOrgRoleBySlug(
            UUID orgId,
            String projectSlug,
            String projectRole,
            String... orgRoles
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            throw new AccessDeniedException("User is not authenticated");
        }

        if ("SYSTEM_ADMIN".equals(userPrincipal.getRole())) {
            return true;
        }

        Project project = projectRepository.findByOrganizationIdAndSlug(orgId, projectSlug)
                .orElseThrow(() -> new AccessDeniedException("Project not found"));

        OrganizationMember orgMember = organizationMemberRepository
                .findByOrganizationIdAndUserId(orgId, userPrincipal.getId())
                .orElseThrow(() -> new AccessDeniedException("User is not a member of the organization"));

        if (orgMember.getStatus() != MemberStatus.ACTIVE) {
            throw new AccessDeniedException("User membership is not active");
        }

        boolean hasAllowedOrgRole = Arrays.stream(orgRoles)
                .anyMatch(role -> role.equals(orgMember.getRole().name()));
        if (hasAllowedOrgRole) {
            return true;
        }

        ProjectMember projectMember = projectMemberRepository
                .findByProjectIdAndUserId(project.getId(), userPrincipal.getId())
                .orElseThrow(() -> new AccessDeniedException("User is not a member of the project"));

        if (projectRole.equals(projectMember.getRole().name())) {
            return true;
        }

        throw new AccessDeniedException("User does not have the required project or organization role");
    }

    public boolean hasProjectRoleOrOrgRoleBySlugAny(
            UUID orgId,
            String projectSlug,
            String... allowedRoles
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            throw new AccessDeniedException("User is not authenticated");
        }

        if ("SYSTEM_ADMIN".equals(userPrincipal.getRole())) {
            return true;
        }

        Project project = projectRepository.findByOrganizationIdAndSlug(orgId, projectSlug)
                .orElseThrow(() -> new AccessDeniedException("Project not found"));

        OrganizationMember orgMember = organizationMemberRepository
                .findByOrganizationIdAndUserId(orgId, userPrincipal.getId())
                .orElseThrow(() -> new AccessDeniedException("User is not a member of the organization"));

        if (orgMember.getStatus() != MemberStatus.ACTIVE) {
            throw new AccessDeniedException("User membership is not active");
        }

        boolean hasAllowedOrgRole = Arrays.stream(allowedRoles)
                .anyMatch(role -> role.equals(orgMember.getRole().name()));
        if (hasAllowedOrgRole) {
            return true;
        }

        ProjectMember projectMember = projectMemberRepository
                .findByProjectIdAndUserId(project.getId(), userPrincipal.getId())
                .orElse(null);

        if (projectMember == null) {
            return false;
        }

        return Arrays.stream(allowedRoles)
                .anyMatch(role -> role.equals(projectMember.getRole().name()));
    }
}
