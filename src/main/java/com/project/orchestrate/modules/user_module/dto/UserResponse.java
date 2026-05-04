package com.project.orchestrate.modules.user_module.dto;

import com.project.orchestrate.modules.user_module.model.User;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String systemRole,
        String status,
        boolean emailVerified
) {
    public static UserResponse from(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getSystemRole() != null ? user.getSystemRole().name() : null,
                user.getStatus() != null ? user.getStatus().name() : null,
                user.isEmailVerified()
        );
    }
}
