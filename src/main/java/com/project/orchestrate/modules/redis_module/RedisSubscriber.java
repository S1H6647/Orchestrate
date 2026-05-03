package com.project.orchestrate.modules.redis_module;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.orchestrate.modules.websocket_module.dto.TaskMoveEvent;
import com.project.orchestrate.modules.websocket_module.dto.TaskCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // JSON String -> Usable Java object
            String json = new String(message.getBody());
            String payload = new String(message.getBody());
            String eventType = objectMapper.readTree(payload).path("eventType").asText(null);

            if ("task.moved".equals(eventType)) {
                TaskMoveEvent event = objectMapper.readValue(payload, TaskMoveEvent.class);
                messagingTemplate.convertAndSend(
                        "/topic/projects/" + event.projectId() + "/tasks",
                        event
                );
                return;
            }

            if ("task.created".equals(eventType)) {
                TaskCreatedEvent event = objectMapper.readValue(payload, TaskCreatedEvent.class);
                messagingTemplate.convertAndSend(
                        "/topic/projects/" + event.projectId() + "/tasks",
                        event
                );
            }

        } catch (JsonMappingException e) {
            log.error("Failed to deserialize edit event from Redis", e);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse edit event from Redis", e);
        }
    }
}
