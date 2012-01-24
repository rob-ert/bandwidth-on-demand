ALTER TABLE reservation DROP COLUMN start_date;
ALTER TABLE reservation DROP COLUMN end_date;
ALTER TABLE reservation DROP COLUMN start_time;
ALTER TABLE reservation DROP COLUMN end_time;

ALTER TABLE reservation ADD COLUMN start_date DATE NOT NULL;
ALTER TABLE reservation ADD COLUMN end_date DATE NOT NULL;
ALTER TABLE reservation ADD COLUMN start_time TIME NOT NULL;
ALTER TABLE reservation ADD COLUMN end_time TIME NOT NULL;