package com.project.orchestrate.modules.auth_module.messaging;

public record AccountVerificationEvent(
        String toEmail,
        String name,
        String token,
        String isVerified
) {
}
