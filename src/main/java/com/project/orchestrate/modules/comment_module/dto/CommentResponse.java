package com.project.orchestrate.modules.comment_module.dto;

import com.project.orchestrate.modules.comment_module.model.Comment;
import com.project.orchestrate.modules.user_module.dto.UserSummary;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        String content,
        UUID taskId,
        UserSummary author,
        boolean edited,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CommentResponse from(Comment comment) {
        if (comment == null) {
            return null;
        }

        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getTask() != null ? comment.getTask().getId() : null,
                UserSummary.from(comment.getAuthor()),
                comment.isEdited(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
