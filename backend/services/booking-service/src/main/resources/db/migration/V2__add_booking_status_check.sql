-- Existing rows are all 'PENDING' -- the entity previously stored status as
-- a free-form string with no server-side enum. This constrains it to the
-- BookingStatus lifecycle now enforced in code.
ALTER TABLE bookings
    ADD CONSTRAINT chk_bookings_status
    CHECK (status IN ('PENDING', 'PAID', 'COMPLETED', 'CANCELLED'));
