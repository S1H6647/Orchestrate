CREATE TABLE labels
(
    id              UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    name            VARCHAR(50) NOT NULL,
    color           VARCHAR(7)  NOT NULL,
    organization_id UUID        NOT NULL REFERENCES organization (id) ON DELETE CASCADE,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_label_org_name UNIQUE (organization_id, name)
);

CREATE INDEX idx_labels_organization_id ON labels (organization_id);