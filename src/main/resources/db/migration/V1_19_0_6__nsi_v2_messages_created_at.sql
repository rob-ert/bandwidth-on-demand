ALTER TABLE nsi_v2_message ADD COLUMN created_at TIMESTAMP WITH TIME ZONE;
UPDATE nsi_v2_message SET created_at = NOW();
ALTER TABLE nsi_v2_message ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE log_event RENAME COLUMN created TO created_at;
