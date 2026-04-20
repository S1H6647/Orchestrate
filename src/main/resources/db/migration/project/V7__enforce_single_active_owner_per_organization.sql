CREATE UNIQUE INDEX IF NOT EXISTS uq_org_members_single_active_owner
ON organization_members (organization_id)
WHERE role = 'OWNER' AND status = 'ACTIVE';
