ALTER TABLE connection ALTER COLUMN description DROP NOT NULL;
ALTER TABLE connection DROP COLUMN minimum_bandwidth;
ALTER TABLE connection DROP COLUMN maximum_bandwidth;

ALTER TABLE connection ADD COLUMN source_stp_id varchar(255);
ALTER TABLE connection ADD COLUMN destination_stp_id varchar(255);

UPDATE connection set source_stp_id = 'urn:dummy:value';
UPDATE connection set destination_stp_id = 'urn:dummy:value';

ALTER TABLE connection ALTER COLUMN source_stp_id SET NOT NULL;
ALTER TABLE connection ALTER COLUMN destination_stp_id SET NOT NULL;