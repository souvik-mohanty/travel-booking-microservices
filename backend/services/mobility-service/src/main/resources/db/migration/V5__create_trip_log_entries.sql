CREATE TABLE trip_log_entries (
    id UUID PRIMARY KEY,

    trip_id UUID NOT NULL,

    type VARCHAR(30) NOT NULL,

    description VARCHAR(1000),
    location VARCHAR(255),

    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,

    -- identity-service user who wrote this entry (the tour's business owner).
    logged_by UUID NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_trip_log_entries_trip
        FOREIGN KEY (trip_id)
        REFERENCES trips(id),

    CONSTRAINT chk_trip_log_entries_type
        CHECK (type IN ('CHECK_IN', 'CHECK_OUT', 'ACTIVITY_COMPLETED', 'DESTINATION_REACHED', 'OTHER'))
);

-- Every read of a trip's logbook fetches the whole thing ordered by when
-- events happened.
CREATE INDEX idx_trip_log_entries_trip_occurred ON trip_log_entries(trip_id, occurred_at);
