-- Create constraint after java migration V0_10_0_8 has run
ALTER TABLE physical_resource_group ADD CONSTRAINT fk_prg_inst FOREIGN KEY (institute_id) REFERENCES institute(id);