ALTER TABLE attendance ALTER COLUMN check_in_time DROP NOT NULL;

ALTER TABLE attendance DROP CONSTRAINT ck_attendance_status;

ALTER TABLE attendance ADD CONSTRAINT ck_attendance_status
    CHECK (status IN ('PRESENT', 'LATE', 'HALF_DAY', 'ABSENT'));