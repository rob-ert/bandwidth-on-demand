ALTER TABLE connection_v1 ADD COLUMN reserve_request_details BIGINT,
                          ADD COLUMN terminate_request_details BIGINT;

ALTER TABLE connection_v1 ADD CONSTRAINT reserve_request_details_fkey FOREIGN KEY (reserve_request_details) REFERENCES nsi_request_details (id);
ALTER TABLE connection_v1 ADD CONSTRAINT terminate_request_details_fkey FOREIGN KEY (terminate_request_details) REFERENCES nsi_request_details (id);
