-- Application-level checks (findByBookingId, findByGatewayOrderId) are
-- subject to a check-then-insert race under concurrent requests; these
-- constraints are the actual guarantee.

-- At most one payment per booking.
ALTER TABLE payments
    ADD CONSTRAINT uq_payments_booking_id UNIQUE (booking_id);

-- A Razorpay order must map to exactly one payment. NULLs (payments not
-- yet assigned an order) are unaffected -- Postgres does not treat NULLs
-- as duplicates under a UNIQUE constraint.
ALTER TABLE payments
    ADD CONSTRAINT uq_payments_gateway_order_id UNIQUE (gateway_order_id);
