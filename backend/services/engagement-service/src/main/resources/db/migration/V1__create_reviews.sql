CREATE TABLE reviews (
    id UUID PRIMARY KEY,

    booking_id UUID NOT NULL,
    activity_id UUID NOT NULL,
    business_id UUID NOT NULL,

    user_id UUID NOT NULL,

    rating INTEGER NOT NULL,
    title VARCHAR(255),
    comment TEXT NOT NULL,

    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT chk_reviews_rating
        CHECK (rating >= 1 AND rating <= 5),

    CONSTRAINT chk_reviews_status
        CHECK (status IN ('ACTIVE', 'HIDDEN', 'DELETED')),

    CONSTRAINT uk_reviews_booking
        UNIQUE (booking_id)
);

CREATE INDEX idx_reviews_activity ON reviews(activity_id);
CREATE INDEX idx_reviews_business ON reviews(business_id);
CREATE INDEX idx_reviews_user ON reviews(user_id);
