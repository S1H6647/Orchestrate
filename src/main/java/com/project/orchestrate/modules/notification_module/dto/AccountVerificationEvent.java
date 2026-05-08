package com.project.orchestrate.modules.notification_module.dto;

public record AccountVerificationEvent(
        String toEmail,
        String name,
        String token,
        String isVerified
) {
}
