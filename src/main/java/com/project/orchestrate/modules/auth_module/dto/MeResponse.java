package com.project.orchestrate.modules.auth_module.dto;

import java.util.UUID;

public record MeResponse(
        UUID id,
        String name,
        String email,
        String avatarUrl,
        String phone,
        String systemRole
) {
}
