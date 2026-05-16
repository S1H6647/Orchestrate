package com.project.orchestrate.modules.comment_module.controller;

import com.project.orchestrate.modules.auth_module.security.user.UserPrincipal;
import com.project.orchestrate.modules.comment_module.dto.CommentResponse;
import com.project.orchestrate.modules.comment_module.dto.CreateCommentRequest;
import com.project.orchestrate.modules.comment_module.dto.UpdateCommentRequest;
import com.project.orchestrate.modules.comment_module.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/projects/{projectSlug}/tasks/{taskId}")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/comments")
    public ResponseEntity<List<CommentResponse>> getAllComments(
            @PathVariable UUID organizationId,
            @PathVariable String projectSlug,
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(commentService.getAllComments(organizationId, projectSlug, taskId, userPrincipal.getUser()));
    }

    @PostMapping("/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable UUID organizationId,
            @PathVariable String projectSlug,
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                commentService.createComment(organizationId, projectSlug, taskId, userPrincipal.getUser(), request));
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable UUID organizationId,
            @PathVariable String projectSlug,
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID commentId,
            @Valid @RequestBody UpdateCommentRequest request
    ) {
        return ResponseEntity.ok(
                commentService.updateComment(organizationId, projectSlug, taskId, userPrincipal.getUser(), commentId, request));
    }
}
