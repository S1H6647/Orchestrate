package com.project.orchestrate.common.config;

import java.util.UUID;

public final class RedisKeys {

    public static final long PRESENCE_TTL_SECONDS = 300;

    private RedisKeys() {
    }

    public static String presenceKey(UUID taskId) {
        return "presence:task:" + taskId;
    }

    public static String taskChannel(UUID taskId) {
        return "task." + taskId;
    }

    public static String projectTasksChannel(UUID projectId) {
        return "projects." + projectId + ".tasks";
    }

    public static String projectTasksChannelPattern() {
        return "projects.*.tasks";
    }

    public static String notificationThrottleKey(UUID taskId, UUID userId) {
        return "notification:throttle:task:" + taskId + ":user:" + userId;
    }
}
