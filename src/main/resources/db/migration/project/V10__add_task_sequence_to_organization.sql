-- Add task_sequence column for auto-incrementing task numbering within an organization
ALTER TABLE organization
    ADD COLUMN IF NOT EXISTS task_sequence BIGINT NOT NULL DEFAULT 0;

COMMENT ON COLUMN organization.task_sequence IS 'Auto-incremented counter for task numbering within this organization';
