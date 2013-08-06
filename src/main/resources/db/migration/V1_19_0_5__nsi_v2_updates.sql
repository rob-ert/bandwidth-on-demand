DELETE FROM notification;
DELETE FROM connection_v2;
DELETE FROM nsi_v2_message;
DELETE FROM nsi_v2_request_details;

ALTER TABLE reservation ALTER COLUMN bandwidth TYPE BIGINT;
ALTER TABLE connection_v1 ALTER COLUMN desired_bandwidth TYPE BIGINT;
ALTER TABLE connection_v2
    ALTER COLUMN desired_bandwidth TYPE BIGINT,
    DROP COLUMN path,
    DROP COLUMN service_attributes,
    ADD COLUMN criteria TEXT NOT NULL;
ALTER TABLE virtual_port ALTER COLUMN max_bandwidth TYPE BIGINT;
ALTER TABLE virtual_port_request_link ALTER COLUMN min_bandwidth TYPE BIGINT;
