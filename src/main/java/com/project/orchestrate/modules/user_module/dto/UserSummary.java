package com.project.orchestrate.modules.user_module.dto;

import java.util.UUID;

public record UserSummary(
        UUID id,
        String name,
        String email
) {
}
