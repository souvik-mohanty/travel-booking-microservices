CREATE TABLE tour_legs (
    id UUID PRIMARY KEY,
    tour_id UUID NOT NULL,
    sequence_order INTEGER NOT NULL,
    destination VARCHAR(255),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    hotel_id UUID NOT NULL,
    room_id UUID NOT NULL,
    rooms_booked INTEGER NOT NULL,
    hotel_reservation_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_tour_legs_tour
        FOREIGN KEY (tour_id)
        REFERENCES tours(id),

    CONSTRAINT chk_tour_legs_dates
        CHECK (start_date < end_date)
);

CREATE INDEX idx_tour_legs_tour_id ON tour_legs(tour_id);


CREATE TABLE tour_activities (
    id UUID PRIMARY KEY,
    tour_id UUID NOT NULL,
    activity_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_tour_activities_tour
        FOREIGN KEY (tour_id)
        REFERENCES tours(id),

    CONSTRAINT uq_tour_activities_tour_activity
        UNIQUE (tour_id, activity_id)
);

CREATE INDEX idx_tour_activities_tour_id ON tour_activities(tour_id);
