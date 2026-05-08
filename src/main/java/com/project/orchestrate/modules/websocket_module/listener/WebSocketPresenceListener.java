package com.project.orchestrate.modules.websocket_module.listener;

import com.project.orchestrate.modules.websocket_module.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketPresenceListener {

    private final PresenceService presenceService;

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {

        if (event.getUser() == null)
            return;

        String userId = event.getUser().getName();
        try {
            log.info("UserId: {}", userId);
            presenceService.markOffline(UUID.fromString(userId));
        } catch (Exception e) {
            log.warn("Invalid userId in disconnect event: {}", userId);
        }
    }

}
