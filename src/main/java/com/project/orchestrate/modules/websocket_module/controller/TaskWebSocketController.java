package com.project.orchestrate.modules.websocket_module.controller;

import com.project.orchestrate.modules.task_module.service.TaskService;
import com.project.orchestrate.modules.websocket_module.dto.TaskMoveEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Objects;
import java.util.UUID;

@Controller
@Slf4j
@RequiredArgsConstructor
public class TaskWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final TaskService taskService;

    @MessageMapping("/task.moved")
    public void handleTaskMove(
            @Payload TaskMoveEvent request,
            SimpMessageHeaderAccessor accessor
    ) {
        UUID userId = extractUserId(accessor);
        String email = extractEmail(accessor);
        
    }

    // Helper
    private UUID extractUserId(SimpMessageHeaderAccessor headerAccessor) {
        UUID userId = (UUID) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("userId");
        if (userId == null) {
            throw new IllegalStateException("No userId in WebSocket session — handshake may have failed");
        }
        return userId;
    }

    private String extractEmail(SimpMessageHeaderAccessor headerAccessor) {
        String email = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("email");
        if (email == null) {
            throw new IllegalStateException("No email in WebSocket session — handshake may have failed");
        }
        return email;
    }
}
