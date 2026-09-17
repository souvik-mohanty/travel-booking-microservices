CREATE TABLE drivers (
    id UUID PRIMARY KEY,

    -- identity-service user this driver profile belongs to.
    user_id UUID NOT NULL,

    license_number VARCHAR(50) NOT NULL,
    license_expiry_date DATE NOT NULL,
    phone VARCHAR(30),

    status VARCHAR(30) NOT NULL,
    availability VARCHAR(20) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uk_drivers_user UNIQUE (user_id),

    CONSTRAINT chk_drivers_status
        CHECK (status IN ('PENDING_VERIFICATION', 'VERIFIED', 'SUSPENDED')),

    CONSTRAINT chk_drivers_availability
        CHECK (availability IN ('AVAILABLE', 'UNAVAILABLE'))
);

CREATE INDEX idx_drivers_status ON drivers(status);
CREATE INDEX idx_drivers_availability ON drivers(availability);
