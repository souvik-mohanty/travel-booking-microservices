CREATE TABLE guides (
    id UUID PRIMARY KEY,

    -- identity-service user this guide profile belongs to.
    user_id UUID NOT NULL,

    bio VARCHAR(2000),
    languages_spoken VARCHAR(255),
    years_of_experience INTEGER,
    phone VARCHAR(30),

    status VARCHAR(30) NOT NULL,
    availability VARCHAR(20) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uk_guides_user UNIQUE (user_id),

    CONSTRAINT chk_guides_status
        CHECK (status IN ('PENDING_VERIFICATION', 'VERIFIED', 'SUSPENDED')),

    CONSTRAINT chk_guides_availability
        CHECK (availability IN ('AVAILABLE', 'UNAVAILABLE'))
);

CREATE INDEX idx_guides_status ON guides(status);
CREATE INDEX idx_guides_availability ON guides(availability);
