package com.project.orchestrate.modules.notification_module.publisher;

import com.project.orchestrate.modules.notification_module.dto.AccountVerificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AccountVerificationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbit.exchange:orchestrate.exchange}")
    private String exchange;

    @Value("${rabbit.routing-key:orchestrate.routingkey}")
    private String routingKey;

    @Value("${rabbit.enabled:false}")
    private boolean rabbitEnabled;

    public void publishAccountVerificationEvent(AccountVerificationEvent event) {
        if (!rabbitEnabled) {
            log.info("RabbitMQ is disabled. Skipping account verification event publish for user: {}", event.name());
            return;
        }

        log.info("Publishing account verification event for user: {}", event.name());

        rabbitTemplate.convertAndSend(exchange, routingKey, event);

        log.info("Event published successfully to exchange: {}", "orchestrate.exchange");
    }
}
