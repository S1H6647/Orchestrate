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
import com.project.orchestrate.modules.project_module.dto.*;
import com.project.orchestrate.modules.project_module.model.Project;
import com.project.orchestrate.modules.project_module.model.ProjectMember;
import com.project.orchestrate.modules.project_module.model.enums.ProjectRole;
import com.project.orchestrate.modules.project_module.model.enums.ProjectStatus;
import com.project.orchestrate.modules.project_module.model.enums.ProjectVisibility;
import com.project.orchestrate.modules.project_module.repository.ProjectMemberRepository;
import com.project.orchestrate.modules.project_module.repository.ProjectRepository;
import com.project.orchestrate.modules.user_module.model.User;
import com.project.orchestrate.modules.user_module.model.enums.MemberStatus;
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

    @Transactional(readOnly = true)
    public ProjectResponse getProjectBySlug(UUID orgId, String projectSlug, UUID userId) {
        assertOrgMember(userId, orgId);

        Project project = projectRepository.findByOrganizationIdAndSlug(orgId, projectSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        OrganizationRole orgRole = getOrgRole(userId, orgId);
        if (!canAccessProject(currentUser, project, orgRole)) {
            throw new AccessDeniedException("You do not have access to this project");
        }

        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID orgId, String projectSlug, @Valid UpdateProjectRequest request, UUID userId) {
        assertOrgMember(userId, orgId);

        Project project = projectRepository.findByOrganizationIdAndSlug(orgId, projectSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (request.name() != null && !request.name().isBlank()) {
            String newName = request.name().trim();
            project.setSlug(newName.equals(project.getName()) ? project.getSlug() : helper.generateSlug(orgId, newName));
            project.setName(newName);
        }
        if (request.description() != null) {
            project.setDescription(request.description());
        }
        if (request.color() != null) {
            project.setColor(request.color());
        }
        if (request.coverImageUrl() != null) {
            project.setCoverImageUrl(request.coverImageUrl());
        }
        if (request.visibility() != null) {
            project.setVisibility(request.visibility());
        }
        if (request.status() != null) {
            project.setStatus(request.status());
        }
        if (request.startDate() != null) {
            project.setStartDate(request.startDate());
        }
        if (request.targetDate() != null) {
            project.setTargetDate(request.targetDate());
        }
        if (request.leadId() != null) {
            User lead = userRepository.findById(request.leadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lead user not found"));
            project.setLead(lead);
        }

        Project saved = projectRepository.save(project);
        return ProjectResponse.from(saved);
    }

    @Transactional
    public void archiveProject(UUID orgId, String projectSlug, UUID userId) {
        assertOrgMember(userId, orgId);

        Project project = projectRepository.findByOrganizationIdAndSlug(orgId, projectSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        project.setStatus(ProjectStatus.ARCHIVED);
        projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getMembers(UUID orgId, String projectSlug, UUID userId) {
        assertOrgMember(userId, orgId);

        Project project = projectRepository.findByOrganizationIdAndSlug(orgId, projectSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        return projectMemberRepository.findAllByProjectId(project.getId())
                .stream()
                .map(ProjectMemberResponse::from)
                .toList();
    }

    @Transactional
    public ProjectMemberResponse addMember(UUID orgId, String projectSlug, @Valid AddProjectMemberRequest request, UUID userId) {
        assertOrgMember(userId, orgId);

        Project project = projectRepository.findByOrganizationIdAndSlug(orgId, projectSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        OrganizationMember orgMember = orgMemberRepository.findByOrganizationIdAndUserId(orgId, request.userId())
                .orElseThrow(() -> new AccessDeniedException("User is not a member of the organization"));

        if (orgMember.getStatus() != MemberStatus.ACTIVE) {
            throw new AccessDeniedException("User membership is not active");
        }

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(project.getId(), request.userId())
                .orElse(ProjectMember.builder()
                        .project(project)
                        .user(user)
                        .build());

        member.setRole(request.role());
        ProjectMember saved = projectMemberRepository.save(member);

        return ProjectMemberResponse.from(saved);
    }

    @Transactional
    public void removeMember(UUID orgId, String projectSlug, UUID targetUserId, UUID userId) {
        assertOrgMember(userId, orgId);

        Project project = projectRepository.findByOrganizationIdAndSlug(orgId, projectSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        projectMemberRepository.deleteByProjectIdAndUserId(project.getId(), targetUserId);
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


    public void deleteProject(UUID organizationId, String projectSlug, @Valid UpdateProjectRequest request, UUID id) {
        assertOrgMember(id, organizationId);

        Project project = projectRepository.findByOrganizationIdAndSlug(organizationId, projectSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        projectRepository.delete(project);
    }
}
