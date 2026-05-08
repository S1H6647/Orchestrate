package com.project.orchestrate.modules.notification_module.consumer;

import com.project.orchestrate.modules.notification_module.dto.AccountVerificationEvent;
import com.project.orchestrate.modules.notification_module.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rabbit.enabled", havingValue = "true")
public class AccountVerificationEventConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = "${rabbit.queue:orchestrate.queue}")
    public void consume(AccountVerificationEvent event) {
        log.info("Received event: isVerified={}, user={}", event.isVerified(), event.name());

        String isVerified = event.isVerified();
        String toEmail = event.toEmail();
        String name = event.name();
        String token = event.token();

        switch (isVerified) {
            case "false" -> emailService.sendVerificationEmail(toEmail, name, token);
            case "true" -> emailService.sendWelcomeEmail(toEmail, name);
        }
    }
}
