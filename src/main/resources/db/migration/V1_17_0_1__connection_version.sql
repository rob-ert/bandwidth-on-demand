ALTER TABLE connection ADD COLUMN type VARCHAR(2);
UPDATE connection SET type = 'V1';
ALTER TABLE connection ALTER COLUMN type SET NOT NULL;
