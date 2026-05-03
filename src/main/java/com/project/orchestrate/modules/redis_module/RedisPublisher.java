package com.project.orchestrate.modules.redis_module;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.orchestrate.common.config.RedisKeys;
import com.project.orchestrate.modules.task_module.model.enums.TaskStatus;
import com.project.orchestrate.modules.task_module.dto.TaskResponse;
import com.project.orchestrate.modules.websocket_module.dto.TaskCreatedEvent;
import com.project.orchestrate.modules.websocket_module.dto.TaskMoveEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisPublisher {
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    public void publishTaskMoveEvent(
            UUID userId,
            String name,
            UUID taskId,
            UUID projectId,
            TaskStatus fromStatus,
            TaskStatus toStatus,
            Double position
    ) {
        try {
            TaskMoveEvent event = new TaskMoveEvent(
                    "task.moved",
                    taskId,
                    projectId,
                    fromStatus,
                    toStatus,
                    position,
                    userId,
                    name,
                    LocalDateTime.now()
            );

            // Java object -> JSON String
            String json = objectMapper.writeValueAsString(event);
            String channel = RedisKeys.projectTasksChannel(projectId);

            redisTemplate.convertAndSend(channel, json);
            
        } catch (JsonProcessingException e) {
            // Don't let serialization failure kill the edit — content is already saved
            log.error("Failed to publish task move event to Redis for task {}", taskId, e);
        } catch (Exception e) {
            log.error("Failed to publish task move event to Redis channel", e);
        }
    }

    public void publishTaskCreatedEvent(
            UUID userId,
            String name,
            UUID projectId,
            TaskResponse task
    ) {
        try {
            TaskCreatedEvent event = new TaskCreatedEvent(
                    "task.created",
                    projectId,
                    task,
                    userId,
                    name,
                    LocalDateTime.now()
            );

            String json = objectMapper.writeValueAsString(event);
            String channel = RedisKeys.projectTasksChannel(projectId);

            redisTemplate.convertAndSend(channel, json);

        } catch (JsonProcessingException e) {
            log.error("Failed to publish task created event to Redis for task {}", task.id(), e);
        } catch (Exception e) {
            log.error("Failed to publish task created event to Redis channel", e);
        }
    }
}
