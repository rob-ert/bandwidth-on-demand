ALTER TABLE connection ADD COLUMN reservation_state VARCHAR(20);
ALTER TABLE connection ADD COLUMN lifecycle_state VARCHAR(20);
ALTER TABLE connection ADD COLUMN provision_state VARCHAR(20);

ALTER TABLE connection ALTER COLUMN current_state DROP NOT NULL;
