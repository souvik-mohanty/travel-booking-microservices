CREATE TABLE payments (

    id UUID PRIMARY KEY,

    booking_id UUID NOT NULL,

    customer_id UUID NOT NULL,

    -- Amount paid by the customer to TourFlow.
    amount NUMERIC(12, 2) NOT NULL,

    currency VARCHAR(3) NOT NULL DEFAULT 'INR',

    -- Customer-side payment state.
    payment_status VARCHAR(30) NOT NULL DEFAULT 'CREATED',

    -- Business payout is deliberately separate from customer payment.
    payout_status VARCHAR(30) NOT NULL DEFAULT 'NOT_ELIGIBLE',

    -- Payment gateway information.
    gateway VARCHAR(30),

    gateway_order_id VARCHAR(255),

    gateway_payment_id VARCHAR(255),

    gateway_signature VARCHAR(512),

    -- Amount TourFlow keeps.
    platform_fee NUMERIC(12, 2),

    -- Amount eventually payable to the business.
    business_amount NUMERIC(12, 2),

    paid_at TIMESTAMP WITH TIME ZONE,

    payout_eligible_at TIMESTAMP WITH TIME ZONE,

    paid_out_at TIMESTAMP WITH TIME ZONE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payments_booking_id
    ON payments(booking_id);

CREATE INDEX idx_payments_customer_id
    ON payments(customer_id);

CREATE INDEX idx_payments_payment_status
    ON payments(payment_status);

CREATE INDEX idx_payments_payout_status
    ON payments(payout_status);
