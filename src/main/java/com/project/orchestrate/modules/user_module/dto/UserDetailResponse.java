package com.project.orchestrate.modules.user_module.dto;

import com.project.orchestrate.modules.user_module.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDetailResponse(
        UUID id,
        String name,
        String email,
        String avatarUrl,
        String phone,
        String systemRole,
        String status,
        boolean emailVerified,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastLoginAt,
        String authProvider
) {
    public static UserDetailResponse from(User user) {
        if (user == null) {
            return null;
        }

        return new UserDetailResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getPhone(),
                user.getSystemRole() != null ? user.getSystemRole().name() : null,
                user.getStatus() != null ? user.getStatus().name() : null,
                user.isEmailVerified(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt(),
                user.getAuthProvider() != null ? user.getAuthProvider().name() : null
        );
    }
}
