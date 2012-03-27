ALTER TABLE reservation ADD COLUMN start_date_time TIMESTAMP;
ALTER TABLE reservation ADD COLUMN end_date_time TIMESTAMP;

UPDATE reservation res set start_date_time = (select start_date + start_time from reservation where id = res.id);
UPDATE reservation res set end_date_time = (select end_date + end_time from reservation where id = res.id);

ALTER TABLE reservation DROP COLUMN start_date;
ALTER TABLE reservation DROP COLUMN start_time;
ALTER TABLE reservation DROP COLUMN end_date;
ALTER TABLE reservation DROP COLUMN end_time;

-- start_date_time is allowed to be null, so a reservation can start NOW
ALTER TABLE reservation  ALTER COLUMN end_date_time SET  NOT NULL;