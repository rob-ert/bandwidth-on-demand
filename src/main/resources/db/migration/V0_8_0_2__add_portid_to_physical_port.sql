ALTER TABLE physical_port add column port_id varchar(255);
UPDATE physical_port pp set port_id = pp.noc_label;
ALTER TABLE physical_port ALTER COLUMN port_id SET NOT NULL;
