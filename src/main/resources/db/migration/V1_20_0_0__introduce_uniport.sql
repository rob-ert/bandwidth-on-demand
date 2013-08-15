ALTER TABLE physical_port ADD COLUMN dtype varchar(100);

UPDATE physical_port SET dtype = 'UniPort';

ALTER TABLE physical_port ALTER COLUMN dtype SET NOT NULL;
