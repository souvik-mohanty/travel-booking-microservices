CREATE TABLE booking_event_log (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL UNIQUE,
    booking_id UUID NOT NULL,
    tour_id UUID NOT NULL,
    user_id UUID NOT NULL,
    total_price NUMERIC(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL
);

-- event_id already has a unique index via the UNIQUE constraint above --
-- BookingEventListener relies on it to make a redelivered BookingConfirmed a
-- safe no-op instead of double-counting the booking.
CREATE INDEX idx_booking_event_log_occurred_at ON booking_event_log(occurred_at);
CREATE INDEX idx_booking_event_log_tour_id ON booking_event_log(tour_id);
