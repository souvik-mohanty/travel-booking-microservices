ALTER TABLE tour_activities
    ADD COLUMN scheduled_date DATE,
    ADD COLUMN scheduled_time TIME;

-- Existing rows (if any) predate scheduling -- backfill to the tour's own
-- start_date so the NOT NULL constraint below can be added safely.
UPDATE tour_activities ta
SET scheduled_date = t.start_date
FROM tours t
WHERE ta.tour_id = t.id
  AND ta.scheduled_date IS NULL;

ALTER TABLE tour_activities
    ALTER COLUMN scheduled_date SET NOT NULL;
