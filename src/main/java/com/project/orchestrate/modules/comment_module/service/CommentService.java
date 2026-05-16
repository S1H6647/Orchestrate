package com.project.orchestrate.modules.comment_module.service;

import com.project.orchestrate.common.exception.AccessDeniedException;
import com.project.orchestrate.common.exception.ResourceNotFoundException;
import com.project.orchestrate.modules.comment_module.dto.CommentResponse;
import com.project.orchestrate.modules.comment_module.dto.CreateCommentRequest;
import com.project.orchestrate.modules.comment_module.dto.UpdateCommentRequest;
import com.project.orchestrate.modules.comment_module.model.Comment;
import com.project.orchestrate.modules.comment_module.repository.CommentRepository;
import com.project.orchestrate.modules.organization_module.model.OrganizationMember;
import com.project.orchestrate.modules.organization_module.model.enums.OrganizationRole;
import com.project.orchestrate.modules.organization_module.repository.OrganizationMemberRepository;
import com.project.orchestrate.modules.project_module.model.Project;
import com.project.orchestrate.modules.project_module.model.enums.ProjectVisibility;
import com.project.orchestrate.modules.project_module.repository.ProjectMemberRepository;
import com.project.orchestrate.modules.project_module.repository.ProjectRepository;
import com.project.orchestrate.modules.task_module.model.Task;
import com.project.orchestrate.modules.task_module.repository.TaskRepository;
import com.project.orchestrate.modules.user_module.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final OrganizationMemberRepository organizationMemberRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public List<CommentResponse> getAllComments(UUID organizationId, String projectSlug, UUID taskId, User user) {
        assertOrganizationMember(user.getId(), organizationId);
        Project project = getProjectOrThrow(organizationId, projectSlug);
        assertCanAccess(user, project);

        Task task = getTaskOrThrow(taskId, project.getId());

        List<Comment> commentList = commentRepository.findAllByTaskId(task.getId());

        return commentList.stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse createComment(
            UUID organizationId,
            String projectSlug,
            UUID taskId,
            User user,
            @Valid CreateCommentRequest request) {
        assertOrganizationMember(user.getId(), organizationId);

        Project project = getProjectOrThrow(organizationId, projectSlug);

        assertCanAccess(user, project);

        Task task = getTaskOrThrow(taskId, project.getId());

        Comment comment = Comment.builder()
                .content(request.content())
                .task(task)
                .author(user)
                .isEdited(false)
                .build();

        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional
    public CommentResponse updateComment(
            UUID organizationId,
            String projectSlug,
            UUID taskId,
            User user,
            UUID commentId,
            @Valid UpdateCommentRequest request) {
        assertOrganizationMember(user.getId(), organizationId);

        Project project = getProjectOrThrow(organizationId, projectSlug);

        assertCanAccess(user, project);

        getTaskOrThrow(taskId, project.getId());

        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        existingComment.setContent(request.content());
        existingComment.setEdited(true);

        return CommentResponse.from(commentRepository.save(existingComment));
    }

    // ── Helpers ────────────────────────────────────────────

    private Project getProjectOrThrow(UUID organizationId, String projectSlug) {
        return projectRepository.findByOrganizationIdAndSlug(organizationId, projectSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private Task getTaskOrThrow(UUID taskId, UUID projectId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // Ensure task belongs to the requested project — prevents cross-project access
        if (!task.getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException("Task not found");
        }

        return task;
    }

    private void assertOrganizationMember(UUID userId, UUID organizationId) {
        if (!organizationMemberRepository.existsByOrganizationIdAndUserId(organizationId, userId)) {
            throw new AccessDeniedException("User is not a member of this organization");
        }
    }

    private void assertCanAccess(User user, Project project) {
        UUID orgId = project.getOrganization().getId();
        OrganizationRole orgRole = organizationMemberRepository
                .findByOrganizationIdAndUserId(orgId, user.getId())
                .map(OrganizationMember::getRole)
                .orElseThrow(() -> new AccessDeniedException("Not an organization member"));

        if (orgRole == OrganizationRole.OWNER || orgRole == OrganizationRole.ADMIN) return;
        if (project.getVisibility() == ProjectVisibility.PUBLIC) return;

        if (!projectMemberRepository.existsByProjectIdAndUserId(
                project.getId(), user.getId())
        ) {
            throw new AccessDeniedException("You do not have access to this project");
        }
    }
}
