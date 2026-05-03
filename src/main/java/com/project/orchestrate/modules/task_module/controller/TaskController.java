package com.project.orchestrate.modules.task_module.controller;

import com.project.orchestrate.modules.auth_module.security.user.UserPrincipal;
import com.project.orchestrate.modules.task_module.dto.*;
import com.project.orchestrate.modules.task_module.model.enums.TaskStatus;
import com.project.orchestrate.modules.task_module.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/projects/{projectSlug}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize(
            "@securityService.hasProjectRoleOrOrgRoleBySlug(#organizationId, #projectSlug, 'MANAGER', 'OWNER', 'ADMIN')"
    )
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable UUID organizationId,
            @PathVariable String projectSlug,
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) throws BadRequestException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(organizationId, projectSlug, request, principal.getUser()));
    }

    @GetMapping
    @PreAuthorize(
            "@securityService.canAccessProjectBySlug(#organizationId, #projectSlug)"
    )
    public ResponseEntity<List<TaskResponse>> getTasks(
            @PathVariable UUID organizationId,
            @PathVariable String projectSlug,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) UUID assigneeId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                taskService.getAllTasks(organizationId, projectSlug, status, assigneeId, principal.getUser())
        );
    }

    @GetMapping("/{taskId}")
    @PreAuthorize(
            "@securityService.canAccessProjectBySlug(#organizationId, #projectSlug)"
    )
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable UUID organizationId,
            @PathVariable String projectSlug,
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                taskService.getTask(organizationId, projectSlug, taskId, principal.getUser())
        );
    }

    @GetMapping("/{taskId}/subtasks")
    @PreAuthorize(
            "@securityService.canAccessProjectBySlug(#organizationId, #projectSlug)"
    )
    public ResponseEntity<List<TaskResponse>> getSubTasks(
            @PathVariable UUID organizationId,
            @PathVariable String projectSlug,
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                taskService.getSubTasks(organizationId, projectSlug, taskId, principal.getUser())
        );
    }

    @PatchMapping("/{taskId}")
    @PreAuthorize(
            "@securityService.hasProjectRoleOrOrgRoleBySlug(#organizationId, #projectSlug, 'MANAGER', 'OWNER', 'ADMIN')"
    )
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID organizationId,
            @PathVariable String projectSlug,
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                taskService.updateTask(organizationId, projectSlug, taskId, request, principal.getUser())
        );
    }

    @PatchMapping("/{taskId}/reorder")
    @PreAuthorize(
            "@securityService.hasProjectRoleOrOrgRoleBySlugAny(#organizationId, #projectSlug, 'MANAGER', 'CONTRIBUTOR', 'OWNER', 'ADMIN')"
    )
    public ResponseEntity<TaskResponse> reorderTask(
            @PathVariable UUID organizationId,
            @PathVariable String projectSlug,
            @PathVariable UUID taskId,
            @Valid @RequestBody ReorderTaskRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                taskService.reorderTask(organizationId, projectSlug, taskId, request, principal.getUser())
        );
    }

    @PatchMapping("/{taskId}/labels")
    @PreAuthorize(
            "@securityService.hasProjectRoleOrOrgRoleBySlug(#organizationId, #projectSlug, 'MANAGER', 'CONTRIBUTOR', 'OWNER', 'ADMIN')"
    )
    public ResponseEntity<Void> addLabel(
            @PathVariable UUID organizationId,
            @PathVariable String projectSlug,
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody AddLabelRequest request
    ) {
        taskService.addLabel(organizationId, projectSlug, taskId, principal.getUser(), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{taskId}/labels/{labelId}")
    @PreAuthorize(
            "@securityService.hasProjectRoleOrOrgRoleBySlug(#organizationId, #projectSlug, 'MANAGER', 'OWNER', 'ADMIN')"
    )
    public ResponseEntity<Void> removeLabel(
            @PathVariable UUID organizationId,
            @PathVariable String projectSlug,
            @PathVariable UUID taskId,
            @PathVariable UUID labelId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        taskService.removeLabel(organizationId, projectSlug, taskId, principal.getUser(), labelId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize(
            "@securityService.hasProjectRoleOrOrgRoleBySlug(#organizationId, #projectSlug, 'MANAGER', 'OWNER', 'ADMIN')"
    )
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID organizationId,
            @PathVariable String projectSlug,
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        taskService.deleteTask(organizationId, projectSlug, taskId, principal.getUser());
        return ResponseEntity.noContent().build();
    }
}
