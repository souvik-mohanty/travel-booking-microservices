ALTER TABLE activities
    ADD COLUMN category VARCHAR(20) NOT NULL DEFAULT 'OTHER';

-- Existing rows (from before this column existed) default to OTHER above;
-- new rows always specify a real category via CreateActivityRequest.
ALTER TABLE activities
    ALTER COLUMN category DROP DEFAULT;
