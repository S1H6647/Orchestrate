package com.project.orchestrate.modules.auth_module.dto;

import com.project.orchestrate.modules.user_module.model.enums.AccountStatus;
import com.project.orchestrate.modules.user_module.model.enums.SystemRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegisterResponse(
    UUID id,
    String name,
    String email,
    SystemRole systemRole,
    AccountStatus status,
    boolean emailVerified,
    LocalDateTime createdAt
) {}