package com.project.orchestrate.modules.notification_module.consumer;

import com.project.orchestrate.modules.notification_module.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rabbit.enabled", havingValue = "true")
public class AccountVerificationEventConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = "orchestrate.queue")
    public void consume(Map<String, String> event) {
        String isVerified = event.get("isVerified");
        log.info("Received event: isVerified={}, user={}", isVerified, event.get("name"));

        String toEmail = event.get("toEmail");
        String name = event.get("name");
        String token = event.get("token");

        switch (isVerified) {
            case "false" -> emailService.sendVerificationEmail(toEmail, name, token);
            case "true" -> emailService.sendWelcomeEmail(toEmail, name);
        }
    }
}
