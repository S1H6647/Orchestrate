package com.project.orchestrate.modules.project_module.controller;

import com.project.orchestrate.modules.auth_module.security.user.UserPrincipal;
import com.project.orchestrate.modules.project_module.dto.CreateProjectRequest;
import com.project.orchestrate.modules.project_module.dto.ProjectResponse;
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
@RequestMapping("/api/v1/organizations/{orgId}/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // GET /api/v1/organizations/{orgId}/projects
    @GetMapping
    @PreAuthorize("@securityService.hasOrgRole(#orgId, 'OWNER', 'ADMIN', 'MEMBER', 'VIEWER')")
    public ResponseEntity<List<ProjectResponse>> getProjects(
            @PathVariable UUID orgId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(projectService.getProjects(orgId, userPrincipal.getId()));
    }

    // POST /api/v1/organizations/{orgId}/projects
    @PostMapping
    @PreAuthorize("@securityService.hasOrgRole(#orgId, 'OWNER', 'ADMIN')")
    public ResponseEntity<ProjectResponse> createProject(
            @PathVariable UUID orgId,
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.createProject(orgId, request, userPrincipal.getId()));
    }

}
