CREATE TABLE IF NOT EXISTS projects (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    color VARCHAR(7) NOT NULL DEFAULT '#6366F1',
    cover_image_url VARCHAR(255),
    type VARCHAR(20) NOT NULL DEFAULT 'BASIC',
    visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNING',
    start_date DATE,
    target_date DATE,
    organization_id UUID NOT NULL,
    created_by UUID NOT NULL,
    lead_id UUID,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    archived_at TIMESTAMP,
    CONSTRAINT fk_projects_organization FOREIGN KEY (organization_id) REFERENCES organization(id),
    CONSTRAINT fk_projects_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_projects_lead_id FOREIGN KEY (lead_id) REFERENCES users(id),
    CONSTRAINT uq_project_org_slug UNIQUE (organization_id, slug),
    CONSTRAINT chk_projects_type CHECK (type IN ('BASIC', 'KANBAN', 'SCRUM')),
    CONSTRAINT chk_projects_visibility CHECK (visibility IN ('PUBLIC', 'PRIVATE')),
    CONSTRAINT chk_projects_status CHECK (status IN ('PLANNING', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'ARCHIVED'))
);

CREATE INDEX IF NOT EXISTS idx_projects_org_id ON projects (organization_id);
CREATE INDEX IF NOT EXISTS idx_projects_created_by ON projects (created_by);
CREATE INDEX IF NOT EXISTS idx_projects_lead_id ON projects (lead_id);
CREATE INDEX IF NOT EXISTS idx_projects_status ON projects (status);
CREATE INDEX IF NOT EXISTS idx_projects_visibility ON projects (visibility);
