package com.project.orchestrate.modules.notification_module.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Value("${spring.mail.username}")
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

        } catch (MessagingException e) {
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

        } catch (MessagingException e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
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
}
