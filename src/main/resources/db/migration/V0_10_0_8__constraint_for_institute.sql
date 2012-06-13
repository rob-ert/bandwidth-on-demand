ALTER TABLE physical_resource_group DROP CONSTRAINT IF EXISTS physical_resource_group_institution_name_key;

ALTER TABLE physical_resource_group DROP CONSTRAINT uniq_institute_id;

ALTER TABLE physical_resource_group ADD CONSTRAINT fk_prg_inst FOREIGN KEY (institute_id) REFERENCES institute(id);