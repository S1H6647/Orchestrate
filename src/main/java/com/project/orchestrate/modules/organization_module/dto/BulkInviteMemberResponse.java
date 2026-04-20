package com.project.orchestrate.modules.organization_module.dto;

import java.util.List;

public record BulkInviteMemberResponse(
        int total,
        int successCount,
        int failureCount,
        List<Result> results
) {
    public record Result(
            String email,
            String role,
            boolean success,
            String message
    ) {
    }
}
