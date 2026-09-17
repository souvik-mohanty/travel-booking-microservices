CREATE TABLE file_metadata (
    id UUID PRIMARY KEY,

    -- identity-service user who uploaded this file.
    owner_id UUID NOT NULL,

    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100),
    size_bytes BIGINT NOT NULL,

    -- Random UUID-based name on local disk under file.storage-path -- never
    -- the original file_name.
    storage_key VARCHAR(255) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_file_metadata_storage_key UNIQUE (storage_key)
);

CREATE INDEX idx_file_metadata_owner_id ON file_metadata(owner_id);
