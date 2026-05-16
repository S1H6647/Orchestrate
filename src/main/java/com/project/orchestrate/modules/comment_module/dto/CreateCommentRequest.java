package com.project.orchestrate.modules.comment_module.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCommentRequest(
        @NotBlank(message = "Content is required")
        @Size(max = 256, message = "Content can not exceed 256 characters")
        String content,

        UUID taskId
) {
}
