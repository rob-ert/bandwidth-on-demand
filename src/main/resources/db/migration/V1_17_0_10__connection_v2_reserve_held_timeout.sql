ALTER TABLE connection_v2 ADD COLUMN reserve_held_timeout_value INTEGER, ADD COLUMN reserve_held_timeout TIMESTAMP WITH TIME ZONE;
UPDATE connection_v2 SET reserve_held_timeout_value = 1200;
ALTER TABLE connection_v2 ALTER COLUMN reserve_held_timeout_value SET NOT NULL;
