package com.project.orchestrate.modules.project_module.controller;

import com.project.orchestrate.modules.auth_module.security.user.UserPrincipal;
import com.project.orchestrate.modules.project_module.dto.*;
import com.project.orchestrate.modules.project_module.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // GET /api/v1/organizations/{organizationId}/projects
    @GetMapping
    @PreAuthorize("@securityService.hasOrgRole(#organizationId, 'OWNER', 'ADMIN', 'MEMBER', 'VIEWER')")
    public ResponseEntity<List<ProjectResponse>> getProjects(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(projectService.getProjects(organizationId, userPrincipal.getId()));
    }

    // POST /api/v1/organizations/{organizationId}/projects
    @PostMapping
    @PreAuthorize("@securityService.hasOrgRole(#organizationId, 'OWNER', 'ADMIN')")
    public ResponseEntity<ProjectResponse> createProject(
            @PathVariable UUID organizationId,
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.createProject(organizationId, request, userPrincipal.getId()));
    }

    // GET /api/v1/organizations/{organizationId}/projects/{projectSlug}
    @GetMapping("/{projectSlug}")
    @PreAuthorize("@securityService.canAccessProjectBySlug(#organizationId, #projectSlug)")
    public ResponseEntity<ProjectResponse> getProjectBySlug(
            @PathVariable UUID organizationId,
            @PathVariable String projectSlug,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(projectService.getProjectBySlug(organizationId, projectSlug, principal.getId()));
    }

    // PATCH /api/v1/organizations/{organizationId}/projects/{projectSlug}
    @PatchMapping("/{projectSlug}")
    @PreAuthorize(
            "@securityService.hasProjectRoleOrOrgRoleBySlug(#organizationId, #projectSlug, 'MANAGER', 'OWNER', 'ADMIN')"
    )
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID organizationId,
            @PathVariable String projectSlug,
            @Valid @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                projectService.updateProject(organizationId, projectSlug, request, principal.getId())
        );
    }

    // DELETE /api/v1/organizations/{organizationId}/projects/{projectSlug}
    @DeleteMapping("/{projectSlug}")
    @PreAuthorize(
            "@securityService.hasProjectRoleOrOrgRoleBySlug(#organizationId, #projectSlug, 'MANAGER', 'OWNER', 'ADMIN')"
    )
    public ResponseEntity<Void> deleteProject(
            @PathVariable UUID organizationId,
            @PathVariable String projectSlug,
            @Valid @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        projectService.deleteProject(organizationId, projectSlug, request, principal.getId());
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/v1/organizations/{organizationId}/projects/{projectSlug}/archive
    @PatchMapping("/{projectSlug}/archive")
    @PreAuthorize(
            "@securityService.hasProjectRoleOrOrgRoleBySlug(#organizationId, #projectSlug, 'MANAGER', 'OWNER', 'ADMIN')"
    )
    public ResponseEntity<Void> archiveProject(
            @PathVariable UUID organizationId,
            @PathVariable String projectSlug,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        projectService.archiveProject(organizationId, projectSlug, principal.getId());
        return ResponseEntity.noContent().build();
    }

    // GET /api/v1/organizations/{organizationId}/projects/{projectSlug}/members
    @GetMapping("/{projectSlug}/members")
    @PreAuthorize("@securityService.canAccessProjectBySlug(#organizationId, #projectSlug)")
    public ResponseEntity<List<ProjectMemberResponse>> getMembers(
            @PathVariable UUID organizationId,
            @PathVariable String projectSlug,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                projectService.getMembers(organizationId, projectSlug, principal.getId())
        );
    }

    // POST /api/v1/organizations/{organizationId}/projects/{projectSlug}/members
    @PostMapping("/{projectSlug}/members")
    @PreAuthorize(
            "@securityService.hasProjectRoleOrOrgRoleBySlug(#organizationId, #projectSlug, 'MANAGER', 'OWNER', 'ADMIN')"
    )
    public ResponseEntity<ProjectMemberResponse> addMember(
            @PathVariable UUID organizationId,
            @PathVariable String projectSlug,
            @Valid @RequestBody AddProjectMemberRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.addMember(organizationId, projectSlug, request, principal.getId()));
    }

    // DELETE /api/v1/organizations/{organizationId}/projects/{projectSlug}/members/{userId}
    @DeleteMapping("/{projectSlug}/members/{userId}")
    @PreAuthorize(
            "@securityService.hasProjectRoleOrOrgRoleBySlug(#organizationId, #projectSlug, 'MANAGER', 'OWNER', 'ADMIN')"
    )
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID organizationId,
            @PathVariable String projectSlug,
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        projectService.removeMember(organizationId, projectSlug, userId, principal.getId());
        return ResponseEntity.noContent().build();
    }

}
