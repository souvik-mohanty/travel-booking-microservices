CREATE TABLE vehicles (
    id UUID PRIMARY KEY,

    -- identity-service user who manages this vehicle (the fleet manager).
    owner_id UUID NOT NULL,

    registration_number VARCHAR(30) NOT NULL,
    type VARCHAR(20) NOT NULL,
    capacity INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uk_vehicles_registration UNIQUE (registration_number),

    CONSTRAINT chk_vehicles_type
        CHECK (type IN ('SEDAN', 'SUV', 'VAN', 'BUS')),

    CONSTRAINT chk_vehicles_status
        CHECK (status IN ('AVAILABLE', 'IN_USE', 'MAINTENANCE', 'INACTIVE'))
);

CREATE INDEX idx_vehicles_owner_id ON vehicles(owner_id);
CREATE INDEX idx_vehicles_status ON vehicles(status);
