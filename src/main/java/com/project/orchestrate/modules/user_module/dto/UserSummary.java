package com.project.orchestrate.modules.user_module.dto;

import com.project.orchestrate.modules.user_module.model.User;

import java.util.UUID;

public record UserSummary(
        UUID id,
        String name,
        String email
) {
    public static UserSummary from(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummary(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
