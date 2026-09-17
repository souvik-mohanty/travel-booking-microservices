CREATE TABLE trip_locations (
    id UUID PRIMARY KEY,

    -- trip-service Trip this location belongs to.
    trip_id UUID NOT NULL,

    latitude NUMERIC(9,6) NOT NULL,
    longitude NUMERIC(9,6) NOT NULL,

    recorded_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT chk_trip_locations_latitude
        CHECK (latitude >= -90 AND latitude <= 90),

    CONSTRAINT chk_trip_locations_longitude
        CHECK (longitude >= -180 AND longitude <= 180)
);

-- Serves both "latest for a trip" and "history for a trip" queries.
CREATE INDEX idx_trip_locations_trip_recorded ON trip_locations(trip_id, recorded_at);
