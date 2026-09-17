CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,

    -- identity-service user who performed the action. Nullable for
    -- system-initiated actions with no human actor.
    actor_id UUID,

    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id UUID,
    details TEXT,

    occurred_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_audit_logs_actor_id ON audit_logs(actor_id);
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource_type, resource_id);
CREATE INDEX idx_audit_logs_occurred_at ON audit_logs(occurred_at);
