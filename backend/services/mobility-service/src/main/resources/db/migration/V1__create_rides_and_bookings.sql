CREATE TABLE rides (
    id UUID PRIMARY KEY,

    -- identity-service user who created this ride (the driver/organizer).
    created_by UUID NOT NULL,

    type VARCHAR(20) NOT NULL,
    pickup_location VARCHAR(255) NOT NULL,
    drop_location VARCHAR(255) NOT NULL,

    total_seats INTEGER NOT NULL,
    available_seats INTEGER NOT NULL,
    price_per_seat NUMERIC(10,2) NOT NULL,

    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT chk_rides_type
        CHECK (type IN ('PRIVATE', 'SHARED', 'FAMILY')),

    CONSTRAINT chk_rides_status
        CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),

    CONSTRAINT chk_rides_seats_bounds
        CHECK (available_seats >= 0 AND available_seats <= total_seats)
);

CREATE INDEX idx_rides_created_by ON rides(created_by);
CREATE INDEX idx_rides_status ON rides(status);


CREATE TABLE ride_bookings (
    id UUID PRIMARY KEY,
    ride_id UUID NOT NULL,

    -- identity-service user who booked seats.
    user_id UUID NOT NULL,

    seats_booked INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_ride_bookings_ride
        FOREIGN KEY (ride_id)
        REFERENCES rides(id),

    CONSTRAINT chk_ride_bookings_status
        CHECK (status IN ('CONFIRMED', 'CANCELLED')),

    CONSTRAINT chk_ride_bookings_seats_positive
        CHECK (seats_booked > 0)
);

CREATE INDEX idx_ride_bookings_ride_id ON ride_bookings(ride_id);
CREATE INDEX idx_ride_bookings_user_id ON ride_bookings(user_id);
