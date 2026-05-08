package com.project.orchestrate.modules.notification_module.model.enums;

public enum TaskType {
    TASK_CREATED,
    TASK_MOVED,
    TASK_DELETED,
    TASK_ASSIGNED,
    TASK_UPDATED,
    LABEL_ADDED,
    LABEL_REMOVED;

    public String toReadable() {
        return switch (this) {
            case TASK_CREATED -> "Task created";
            case TASK_MOVED -> "Task moved";
            case TASK_DELETED -> "Task deleted";
            case TASK_ASSIGNED -> "Task assigned";
            case TASK_UPDATED -> "Task updated";
            case LABEL_ADDED -> "Label added";
            case LABEL_REMOVED -> "Label removed";
        };
    }
}
