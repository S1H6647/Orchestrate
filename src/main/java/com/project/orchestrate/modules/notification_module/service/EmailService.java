package com.project.orchestrate.modules.notification_module.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.mail.from:${spring.mail.username}}")
    private String fromEmail;

    // ── Verification Email ─────────────────────────────────
    @Async
    public void sendVerificationEmail(String toEmail, String name, String token) {
        try {
            String verificationLink = baseUrl + "/api/v1/auth/verify?token=" + token;
            String resendVerificationLink = baseUrl + "/api/v1/auth/resend-verification?email=" +
                    URLEncoder.encode(toEmail, StandardCharsets.UTF_8);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verify your email — PMS");
            helper.setText(buildVerificationEmailBody(name, verificationLink, resendVerificationLink), true); // true = HTML

            mailSender.send(message);
            log.info("Verification email sent to {}", toEmail);

        } catch (MessagingException | MailException e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
            // Don't rethrow — email failure should not crash registration
            // User can always use resend endpoint
        }
    }

    // ── Welcome Email ──────────────────────────────────────
    @Async
    public void sendWelcomeEmail(String toEmail, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to PMS");
            helper.setText(buildWelcomeEmailBody(name), true);

            mailSender.send(message);
            log.info("Welcome email sent to {}", toEmail);

        } catch (MessagingException | MailException e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
        }
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
        try {
            String inviteLink = baseUrl + "/api/v1/organizations/invitations/accept?token=" +
                    URLEncoder.encode(inviteToken, StandardCharsets.UTF_8);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("You're invited to join " + organizationName + " - PMS");
            helper.setText(
                    buildOrganizationInvitationEmailBody(
                            inviteeName,
                            inviterName,
                            organizationName,
                            role,
                            inviteLink
                    ),
                    true
            );

            mailSender.send(message);
            log.info("Organization invitation email sent to {} for organization {}", toEmail, organizationName);

        } catch (MessagingException | MailException e) {
            log.error("Failed to send organization invitation email to {}: {}", toEmail, e.getMessage());
        }
    }

    // ── Organization Deletion Email ───────────────────────
    @Async
    public void sendOrganizationDeletionEmail(
            String toEmail,
            String ownerName,
            String organizationName
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Organization deleted: " + organizationName + " - PMS");
            helper.setText(buildOrganizationDeletionEmailBody(ownerName, organizationName), true);

            mailSender.send(message);
            log.info("Organization deletion email sent to {} for organization {}", toEmail, organizationName);

        } catch (MessagingException | MailException e) {
            log.error("Failed to send organization deletion email to {}: {}", toEmail, e.getMessage());
        }
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
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Ownership transferred: " + organizationName + " - PMS");
            helper.setText(
                    buildOwnershipTransferredFromOwnerEmailBody(
                            previousOwnerName,
                            organizationName,
                            organizationSlug,
                            newOwnerName,
                            newOwnerEmail
                    ),
                    true
            );

            mailSender.send(message);
            log.info("Ownership transfer email sent to previous owner {} for organization {}", toEmail, organizationName);

        } catch (MessagingException | MailException e) {
            log.error("Failed to send ownership transfer email to previous owner {}: {}", toEmail, e.getMessage());
        }
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
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("You are now owner: " + organizationName + " - PMS");
            helper.setText(
                    buildOwnershipTransferredToOwnerEmailBody(
                            newOwnerName,
                            organizationName,
                            organizationSlug,
                            previousOwnerName,
                            previousOwnerEmail
                    ),
                    true
            );

            mailSender.send(message);
            log.info("Ownership transfer email sent to new owner {} for organization {}", toEmail, organizationName);

        } catch (MessagingException | MailException e) {
            log.error("Failed to send ownership transfer email to new owner {}: {}", toEmail, e.getMessage());
        }
    }

    // ── Email Templates ────────────────────────────────────
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

    private String safeText(String value) {
        return (value == null || value.isBlank()) ? "N/A" : value;
    }
}
