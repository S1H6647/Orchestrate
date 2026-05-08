package com.project.orchestrate.modules.notification_module.service;

import com.project.orchestrate.modules.notification_module.dto.BrevoEmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final WebClient brevoWebClient;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.mail.from:${spring.mail.username}}")
    private String fromEmail;

    // ── Verification Email ─────────────────────────────────
    @Async
    public void sendVerificationEmail(
            String toEmail,
            String name,
            String token
    ) {
        String verificationLink = baseUrl + "/verify?token=" + token;

        String resendVerificationLink = baseUrl + "/resend-verification?email=" +
                URLEncoder.encode(toEmail, StandardCharsets.UTF_8);

        String html = buildVerificationEmailBody(
                name,
                verificationLink,
                resendVerificationLink
        );

        sendEmailWithLogging(
                toEmail,
                name,
                "Verify your email — Orchestrate",
                html
        );

        log.info("Verification email was sent to {}", toEmail);
    }

    // ── Welcome Email ──────────────────────────────────────
    @Async
    public void sendWelcomeEmail(String toEmail, String name) {
        String html = buildWelcomeEmailBody(name);

        sendEmailWithLogging(
                toEmail,
                name,
                "Welcome to Orchestrate",
                html
        );
    }

    // ── Organization Invitation Email ─────────────────────
    @Async
    public void sendOrganizationInvitationEmail(
            String toEmail,
            String inviteeName,
            String inviterName,
            String organizationName,
            String role,
            String inviteToken
    ) {
        String inviteLink = baseUrl + "/api/v1/organizations/invitations/accept?token=" +
                URLEncoder.encode(inviteToken, StandardCharsets.UTF_8);

        String html = buildOrganizationInvitationEmailBody(
                inviteeName,
                inviterName,
                organizationName,
                role,
                inviteLink
        );

        sendEmailWithLogging(
                toEmail,
                inviteeName,
                "You have been invited to join Orchestrate",
                html
        );
    }

    // ── Organization Deletion Email ───────────────────────
    @Async
    public void sendOrganizationDeletionEmail(
            String toEmail,
            String ownerName,
            String organizationName
    ) {
        String html = buildOrganizationDeletionEmailBody(ownerName, organizationName);

        sendEmailWithLogging(
                toEmail,
                ownerName,
                "Organization deleted — Orchestrate",
                html
        );

    }

    // ── Organization Ownership Transfer Emails ─────────────
    @Async
    public void sendOwnershipTransferredToPreviousOwnerEmail(
            String toEmail,
            String previousOwnerName,
            String organizationName,
            String organizationSlug,
            String newOwnerName,
            String newOwnerEmail
    ) {
        String html = buildOwnershipTransferredFromOwnerEmailBody(
                previousOwnerName,
                organizationName,
                organizationSlug,
                newOwnerName,
                newOwnerEmail
        );

        sendEmailWithLogging(
                toEmail,
                previousOwnerName,
                "Ownership transferred — Orchestrate",
                html
        );
    }

    @Async
    public void sendOwnershipTransferredToNewOwnerEmail(
            String toEmail,
            String newOwnerName,
            String organizationName,
            String organizationSlug,
            String previousOwnerName,
            String previousOwnerEmail
    ) {
        String html = buildOwnershipTransferredToOwnerEmailBody(
                newOwnerName,
                organizationName,
                organizationSlug,
                previousOwnerName,
                previousOwnerEmail
        );

        sendEmailWithLogging(
                toEmail,
                newOwnerName,
                "Ownership transferred — Orchestrate",
                html
        );
    }

    private void sendEmailWithLogging(
            String toEmail,
            String toName,
            String subject,
            String htmlContent
    ) {
        sendEmail(
                toEmail,
                toName,
                subject,
                htmlContent
        )
                .timeout(Duration.ofSeconds(20))
                .doOnSuccess(unused -> log.info("Email sent to {}", toEmail))
                .doOnError(error -> log.error("Email failed to {}: {}", toEmail, error.getMessage()))
                .subscribe();
    }

    private Mono<Void> sendEmail(
            String toEmail,
            String toName,
            String subject,
            String htmlContent
    ) {

        BrevoEmailRequest.Sender sender = BrevoEmailRequest.Sender.builder()
                .name("Orchestrate")
                .email(fromEmail)
                .build();

        BrevoEmailRequest.Recipient recipient = BrevoEmailRequest.Recipient.builder()
                .name(toName != null ? toName : "User")
                .email(toEmail)
                .build();

        BrevoEmailRequest payload = BrevoEmailRequest.builder()
                .sender(sender)
                .to(List.of(recipient))
                .subject(subject)
                .htmlContent(htmlContent)
                .build();

        return brevoWebClient.post()
                .uri("/smtp/email")
                .bodyValue(payload)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response -> response.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Client error: " + body))
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response -> response.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Server error: " + body))
                )
                .bodyToMono(Void.class);
    }

    // ── Email Templates ────────────────────────────────────
    @Async
    public void sendTaskNotificationEmail(
            String toEmail,
            String recipientName,
            String actorName,
            String projectName,
            String taskTitle,
            String actionLabel,
            String detailLine,
            String taskUrl
    ) {
        String html = buildTaskNotificationEmailBody(
                recipientName,
                actorName,
                projectName,
                taskTitle,
                actionLabel,
                detailLine,
                taskUrl
        );

        sendEmailWithLogging(
                toEmail,
                recipientName,
                actionLabel + " — Orchestrate",
                html
        );
    }

    private String buildVerificationEmailBody(String name, String link, String resendLink) {
        return """
                <html>
                  <body style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                    <h2>Hi %s</h2>
                    <p>Thanks for signing up! Please verify your email address to activate your account.</p>
                    <p>This link expires in <strong>24 hours</strong>.</p>
                    <a href="%s"
                       style="display:inline-block; padding:12px 24px; background:#6366F1;
                              color:white; border-radius:6px; text-decoration:none;
                              font-weight:bold;">
                      Verify Email
                    </a>
                    <p style="margin-top:24px; color:#888; font-size:12px;">
                      If you didn't create an account, you can safely ignore this email.
                    </p>
                    <p style="margin-top:16px;">Didn't get a valid link?</p>
                    <a href="%s"
                       style="display:inline-block; padding:10px 20px; background:#334155;
                              color:white; border-radius:6px; text-decoration:none;
                              font-weight:bold;">
                      Resend Verification
                    </a>
                  </body>
                </html>
                """.formatted(name, link, resendLink);
    }

    private String buildWelcomeEmailBody(String name) {
        return """
                <html>
                  <body style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                    <h2>Welcome aboard, %s!</h2>
                    <p>Your email has been verified. You can now log in and start managing your projects.</p>
                    <a href="%s/login"
                       style="display:inline-block; padding:12px 24px; background:#6366F1;
                              color:white; border-radius:6px; text-decoration:none;
                              font-weight:bold;">
                      Go to Dashboard
                    </a>
                  </body>
                </html>
                """.formatted(name, baseUrl);
    }

    private String buildOrganizationInvitationEmailBody(
            String inviteeName,
            String inviterName,
            String organizationName,
            String role,
            String inviteLink
    ) {
        String recipient = (inviteeName == null || inviteeName.isBlank()) ? "there" : inviteeName;

        return """
                <html>
                  <body style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                    <h2>Hi %s</h2>
                    <p><strong>%s</strong> invited you to join <strong>%s</strong> on PMS.</p>
                    <p>Your role will be <strong>%s</strong>.</p>
                    <p>This invitation link expires in <strong>48 hours</strong>.</p>
                    <a href="%s"
                       style="display:inline-block; padding:12px 24px; background:#6366F1;
                              color:white; border-radius:6px; text-decoration:none;
                              font-weight:bold;">
                      Accept Invitation
                    </a>
                    <p style="margin-top:24px; color:#888; font-size:12px;">
                      If you were not expecting this invitation, you can safely ignore this email.
                    </p>
                  </body>
                </html>
                """.formatted(recipient, inviterName, organizationName, role, inviteLink);
    }

    private String buildOrganizationDeletionEmailBody(String ownerName, String organizationName) {
        String recipient = (ownerName == null || ownerName.isBlank()) ? "there" : ownerName;

        return """
                <html>
                  <body style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                    <h2>Hi %s</h2>
                    <p>This is a confirmation that your organization <strong>%s</strong> has been permanently deleted.</p>
                    <p>All associated data is no longer accessible.</p>
                    <p style="margin-top:24px; color:#888; font-size:12px;">
                      If this was not initiated by you, contact support immediately.
                    </p>
                  </body>
                </html>
                """.formatted(recipient, organizationName);
    }

    private String buildOwnershipTransferredFromOwnerEmailBody(
            String previousOwnerName,
            String organizationName,
            String organizationSlug,
            String newOwnerName,
            String newOwnerEmail
    ) {
        String recipient = (previousOwnerName == null || previousOwnerName.isBlank()) ? "there" : previousOwnerName;
        String nextOwner = (newOwnerName == null || newOwnerName.isBlank()) ? "the selected member" : newOwnerName;

        return """
                <html>
                  <body style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                    <h2>Hi %s</h2>
                    <p>You have transferred ownership of <strong>%s</strong>.</p>
                    <p><strong>Organization slug:</strong> %s</p>
                    <p><strong>New owner:</strong> %s (%s)</p>
                    <p style="margin-top:24px; color:#888; font-size:12px;">
                      If this was not initiated by you, contact support immediately.
                    </p>
                  </body>
                </html>
                """.formatted(recipient, organizationName, organizationSlug, nextOwner, safeText(newOwnerEmail));
    }

    private String buildOwnershipTransferredToOwnerEmailBody(
            String newOwnerName,
            String organizationName,
            String organizationSlug,
            String previousOwnerName,
            String previousOwnerEmail
    ) {
        String recipient = (newOwnerName == null || newOwnerName.isBlank()) ? "there" : newOwnerName;
        String formerOwner = (previousOwnerName == null || previousOwnerName.isBlank()) ? "the previous owner" : previousOwnerName;

        return """
                <html>
                  <body style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                    <h2>Hi %s</h2>
                    <p>You are now the owner of <strong>%s</strong>.</p>
                    <p><strong>Organization slug:</strong> %s</p>
                    <p><strong>Previous owner:</strong> %s (%s)</p>
                    <p>You now have full owner access and permissions in this organization.</p>
                  </body>
                </html>
                """.formatted(recipient, organizationName, organizationSlug, formerOwner, safeText(previousOwnerEmail));
    }

    private String buildTaskNotificationEmailBody(
            String recipientName,
            String actorName,
            String projectName,
            String taskTitle,
            String actionLabel,
            String detailLine,
            String taskUrl
    ) {
        String recipient = (recipientName == null || recipientName.isBlank()) ? "there" : recipientName;
        String actor = (actorName == null || actorName.isBlank()) ? "Someone" : actorName;
        String project = (projectName == null || projectName.isBlank()) ? "your project" : projectName;
        String title = (taskTitle == null || taskTitle.isBlank()) ? "a task" : taskTitle;
        String detail = (detailLine == null || detailLine.isBlank()) ? "" : detailLine;
        String link = (taskUrl == null || taskUrl.isBlank()) ? baseUrl : taskUrl;

        return """
                <html>
                  <body style="font-family: Arial, sans-serif; max-width: 640px; margin: auto;">
                    <h2>Hi %s</h2>
                    <p><strong>%s</strong> %s in <strong>%s</strong>.</p>
                    <div style="margin:16px 0; padding:12px 16px; border:1px solid #E2E8F0; border-radius:8px;">
                      <p style="margin:0 0 6px 0; font-size:12px; color:#64748B;">Task</p>
                      <p style="margin:0; font-size:15px; font-weight:600;">%s</p>
                    </div>
                    %s
                    <a href="%s"
                       style="display:inline-block; padding:12px 24px; background:#0F172A;
                              color:white; border-radius:6px; text-decoration:none;
                              font-weight:bold; margin-top:12px;">
                      Open in Orchestrate
                    </a>
                    <p style="margin-top:20px; color:#94A3B8; font-size:12px;">
                      You are receiving this because you are a member of %s.
                    </p>
                  </body>
                </html>
                """.formatted(
                recipient,
                actor,
                actionLabel,
                project,
                title,
                detail.isBlank() ? "" : "<p style=\"color:#334155; font-size:13px; margin:0 0 12px 0;\">" + detail + "</p>",
                link,
                project
        );
    }

    private String safeText(String value) {
        return (value == null || value.isBlank()) ? "N/A" : value;
    }
}
