CREATE TABLE IF NOT EXISTS organization (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    logo_url VARCHAR(255),
    website_url VARCHAR(255),
    plan VARCHAR(20) NOT NULL DEFAULT 'FREE',
    max_members INT NOT NULL DEFAULT 5,
    max_projects INT NOT NULL DEFAULT 3,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_organization_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT chk_organization_plan CHECK (plan IN ('FREE', 'PRO', 'ENTERPRISE')),
    CONSTRAINT chk_organization_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED')),
    CONSTRAINT chk_organization_limits CHECK (max_members > 0 AND max_projects > 0)
);

CREATE INDEX IF NOT EXISTS idx_organization_created_by ON organization (created_by);
CREATE INDEX IF NOT EXISTS idx_organization_status ON organization (status);

