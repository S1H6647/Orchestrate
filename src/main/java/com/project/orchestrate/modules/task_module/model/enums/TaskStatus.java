package com.project.orchestrate.modules.task_module.model.enums;

public enum TaskStatus {
    BACKLOG,        // not yet planned
    TODO,           // planned, not started
    IN_PROGRESS,    // being worked on
    IN_REVIEW,      // PR/review stage
    DONE,           // completed
    CANCELLED       // won't do
}
