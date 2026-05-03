CREATE TABLE tasks
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    identifier     VARCHAR(20)  NOT NULL,
    title          VARCHAR(255) NOT NULL,
    description    TEXT,
    status         VARCHAR(20)  NOT NULL DEFAULT 'BACKLOG',
    priority       VARCHAR(20)  NOT NULL DEFAULT 'NO_PRIORITY',
    position       FLOAT        NOT NULL DEFAULT 0.0,
    due_date       DATE,
    completed_at   TIMESTAMP,
    story_points   INT,
    project_id     UUID         NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    assignee_id    UUID         REFERENCES users (id) ON DELETE SET NULL,
    reporter_id    UUID         NOT NULL REFERENCES users (id),
    parent_task_id UUID REFERENCES tasks (id) ON DELETE CASCADE,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tasks_project_id ON tasks (project_id);
CREATE INDEX idx_tasks_assignee_id ON tasks (assignee_id);
CREATE INDEX idx_tasks_reporter_id ON tasks (reporter_id);
CREATE INDEX idx_tasks_status ON tasks (status);
CREATE INDEX idx_tasks_priority ON tasks (priority);
CREATE INDEX idx_tasks_parent_task_id ON tasks (parent_task_id);
CREATE INDEX idx_tasks_identifier ON tasks (identifier);
