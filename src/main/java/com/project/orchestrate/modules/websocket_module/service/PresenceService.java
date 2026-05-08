package com.project.orchestrate.modules.websocket_module.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final StringRedisTemplate redisTemplate;
    private final Duration ttl = Duration.ofSeconds(90);

    public void markOnline(UUID userId) {
        String key = key(userId);
        Long count = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, ttl);
    }

    public void markOffline(UUID userId) {
        String key = key(userId);
        Long count = redisTemplate.opsForValue().increment(key);

        if (count == null || count <= 0)
            redisTemplate.delete(key(userId));
    }

    public boolean isOnline(UUID userId) {
        String value = redisTemplate.opsForValue().get(key(userId));
        if (value == null) return false;

        try {
            return Integer.parseInt(value) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String key(UUID id) {
        return "presence:user:" + id;
    }
}
