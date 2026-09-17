CREATE TABLE room_reservations (
    id UUID PRIMARY KEY,
    room_id UUID NOT NULL,
    tour_id UUID NOT NULL,
    tour_leg_id UUID,
    reserved_by UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    rooms_reserved INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_room_reservations_room
        FOREIGN KEY (room_id)
        REFERENCES rooms(id),

    CONSTRAINT chk_room_reservations_dates
        CHECK (start_date < end_date)
);

-- Every availability check sums ACTIVE reservations for one room overlapping
-- a date range -- this index carries that query's entire WHERE clause.
CREATE INDEX idx_room_reservations_room_dates
    ON room_reservations(room_id, status, start_date, end_date);

CREATE INDEX idx_room_reservations_tour_id
    ON room_reservations(tour_id);
