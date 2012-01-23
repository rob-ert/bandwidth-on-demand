ALTER TABLE physical_resource_group ADD COLUMN institute_id BIGINT NOT NULL;
ALTER TABLE physical_resource_group DROP COLUMN institution_name;
ALTER TABLE physical_resource_group DROP COLUMN name;
