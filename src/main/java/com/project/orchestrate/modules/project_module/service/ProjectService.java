package com.project.orchestrate.modules.project_module.service;

import com.project.orchestrate.common.exception.AccessDeniedException;
import com.project.orchestrate.common.exception.PlanLimitExceededException;
import com.project.orchestrate.common.exception.ResourceNotFoundException;
import com.project.orchestrate.common.service.HelperMethodService;
import com.project.orchestrate.modules.organization_module.model.Organization;
import com.project.orchestrate.modules.organization_module.model.OrganizationMember;
import com.project.orchestrate.modules.organization_module.model.enums.OrganizationRole;
import com.project.orchestrate.modules.organization_module.repository.OrganizationMemberRepository;
import com.project.orchestrate.modules.organization_module.repository.OrganizationRepository;
import com.project.orchestrate.modules.project_module.dto.CreateProjectRequest;
import com.project.orchestrate.modules.project_module.dto.ProjectResponse;
import com.project.orchestrate.modules.project_module.model.Project;
import com.project.orchestrate.modules.project_module.model.ProjectMember;
import com.project.orchestrate.modules.project_module.model.enums.ProjectRole;
import com.project.orchestrate.modules.project_module.model.enums.ProjectStatus;
import com.project.orchestrate.modules.project_module.model.enums.ProjectVisibility;
import com.project.orchestrate.modules.project_module.repository.ProjectMemberRepository;
import com.project.orchestrate.modules.project_module.repository.ProjectRepository;
import com.project.orchestrate.modules.user_module.model.User;
import com.project.orchestrate.modules.user_module.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository orgMemberRepository;
    private final HelperMethodService helper;

    @Transactional
    public ProjectResponse createProject(
            UUID orgId,
            @Valid CreateProjectRequest request,
            UUID userId) {

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        assertOrgMember(userId, orgId);

        long currentCount = projectRepository.findAllByOrganizationId(orgId).size();
        if (currentCount >= org.getMaxProjects()) {
            throw new PlanLimitExceededException("Organization has reached the maximum number of projects allowed by the current plan");
        }

        Project project = Project.builder()
                .name(request.name())
                .slug(helper.generateSlug(orgId, request.name()))
                .description(request.description())
                .color(request.color() != null ? request.color() : "#6366F1")
                .type(request.type())
                .visibility(request.visibility())
                .status(ProjectStatus.PLANNING)
                .startDate(request.startDate())
                .targetDate(request.targetDate())
                .organization(org)
                .createdBy(currentUser)
                .lead(currentUser)
                .build();

        projectRepository.save(project);

        ProjectMember membership = ProjectMember.builder()
                .project(project)
                .user(currentUser)
                .role(ProjectRole.MANAGER)
                .build();

        projectMemberRepository.save(membership);

        log.info("Project created: {} in org: {}", project.getSlug(), org.getSlug());

        return ProjectResponse.from(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjects(UUID orgId, UUID userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        assertOrgMember(userId, orgId);

        OrganizationRole orgRole = getOrgRole(userId, orgId);

        List<Project> projectList = projectRepository.findAllByOrganizationId(orgId);

        return projectList.stream()
                .filter(project -> canAccessProject(currentUser, project, orgRole))
                .map(ProjectResponse::from)
                .toList();
    }

    // ── Access Control Helpers ─────────────────────────────

    private void assertOrgMember(UUID userId, UUID organizationId) {
        if (!orgMemberRepository.existsByOrganizationIdAndUserId(organizationId, userId)) {
            throw new AccessDeniedException("You are not a member of this organization");
        }
    }

    private boolean canAccessProject(User user, Project project, OrganizationRole orgRole) {
        if (orgRole == OrganizationRole.OWNER || orgRole == OrganizationRole.ADMIN)
            return true;

        if (project.getVisibility() == ProjectVisibility.PUBLIC)
            return true;

        return projectMemberRepository.existsByProjectIdAndUserId(user.getId(), project.getId());
    }

    private OrganizationRole getOrgRole(UUID userId, UUID orgId) {
        return orgMemberRepository.findByOrganizationIdAndUserId(orgId, userId)
                .map(OrganizationMember::getRole)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this organization"));
    }


}
