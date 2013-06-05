ALTER TABLE connection ALTER COLUMN service_parameters DROP NOT NULL;
ALTER TABLE connection ADD COLUMN service_attributes TEXT;
