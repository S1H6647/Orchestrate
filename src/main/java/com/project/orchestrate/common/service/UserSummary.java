package com.project.orchestrate.common.service;

import com.project.orchestrate.modules.user_module.model.User;

import java.util.UUID;

public record UserSummary(
        UUID id,
        String name,
        String email,
        String avatarUrl
) {
    public static UserSummary from(User user) {
        return new UserSummary(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAvatarUrl()
        );
    }
}

