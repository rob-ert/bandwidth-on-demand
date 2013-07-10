ALTER TABLE connection_v2 RENAME COLUMN reserve_request_details TO initial_reserve_request_details;
ALTER TABLE connection_v2 ADD COLUMN last_reservation_request_details BIGINT,
                          ADD COLUMN last_lifecycle_request_details BIGINT,
                          ADD COLUMN last_provision_request_details BIGINT;

ALTER TABLE connection_v2 ADD CONSTRAINT last_reservation_request_details_fkey FOREIGN KEY (last_reservation_request_details) REFERENCES nsi_request_details (id);
ALTER TABLE connection_v2 ADD CONSTRAINT last_lifecycle_request_details_fkey FOREIGN KEY (last_lifecycle_request_details) REFERENCES nsi_request_details (id);
ALTER TABLE connection_v2 ADD CONSTRAINT last_provision_request_details_fkey FOREIGN KEY (last_provision_request_details) REFERENCES nsi_request_details (id);
