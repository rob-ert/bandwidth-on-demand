ALTER TABLE reservation ADD COLUMN creation_date_time TIMESTAMP;

UPDATE reservation res set creation_date_time = (select start_date from reservation where id = res.id);

ALTER TABLE reservation  ALTER COLUMN creation_date_time SET  NOT NULL;

