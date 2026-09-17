CREATE TABLE trips (
    id UUID PRIMARY KEY,

    -- The booking (booking-service) this trip executes. One trip per booking.
    booking_id UUID NOT NULL,

    -- identity-service user who created/manages this trip.
    created_by UUID NOT NULL,

    -- driver-service driver, assigned later.
    driver_id UUID,

    -- fleet-service vehicle, assigned later.
    vehicle_id UUID,

    status VARCHAR(20) NOT NULL,

    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uk_trips_booking UNIQUE (booking_id),

    CONSTRAINT chk_trips_status
        CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_trips_created_by ON trips(created_by);
CREATE INDEX idx_trips_status ON trips(status);
