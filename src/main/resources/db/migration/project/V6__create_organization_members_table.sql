CREATE TABLE IF NOT EXISTS organization_members (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    invite_token VARCHAR(64),
    invite_expires_at TIMESTAMP,
    joined_at TIMESTAMP,
    CONSTRAINT fk_org_members_organization FOREIGN KEY (organization_id) REFERENCES organization(id),
    CONSTRAINT fk_org_members_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_org_members_org_user UNIQUE (organization_id, user_id),
    CONSTRAINT uq_org_members_invite_token UNIQUE (invite_token),
    CONSTRAINT chk_org_members_role CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER', 'VIEWER')),
    CONSTRAINT chk_org_members_status CHECK (status IN ('ACTIVE', 'INVITED', 'REMOVED'))
);

CREATE INDEX IF NOT EXISTS idx_org_members_org_id ON organization_members (organization_id);
CREATE INDEX IF NOT EXISTS idx_org_members_user_id ON organization_members (user_id);
CREATE INDEX IF NOT EXISTS idx_org_members_status ON organization_members (status);
