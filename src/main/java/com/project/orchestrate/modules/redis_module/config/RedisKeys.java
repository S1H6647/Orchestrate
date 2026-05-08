package com.project.orchestrate.modules.redis_module.config;

import java.util.UUID;

public final class RedisKeys {

    private RedisKeys() {
    }

    public static String projectTasksChannel(UUID projectId) {
        return "projects." + projectId + ".tasks";
    }

    public static String projectTasksChannelPattern() {
        return "projects.*.tasks";
    }

}
