ALTER TABLE nsi_request_details RENAME TO nsi_v1_request_details;

CREATE TABLE nsi_v2_request_details (
    id BIGINT NOT NULL PRIMARY KEY,
    reply_to TEXT,
    correlation_id CHARACTER VARYING(255) NOT NULL,
    requester_nsa TEXT NOT NULL,
    provider_nsa TEXT NOT NULL);

INSERT INTO nsi_v2_request_details (id, reply_to, correlation_id, requester_nsa, provider_nsa) 
  SELECT id, reply_to, correlation_id, requester_nsa, provider_nsa
    FROM nsi_v1_request_details
   WHERE requester_nsa IS NOT NULL AND provider_nsa IS NOT NULL;

ALTER TABLE connection_v2
    DROP CONSTRAINT connection_v2_provision_request_details_fkey,
    DROP CONSTRAINT last_reservation_request_details_fkey,
    DROP CONSTRAINT last_lifecycle_request_details_fkey,
    DROP CONSTRAINT last_provision_request_details_fkey;

ALTER TABLE connection_v2
    ADD CONSTRAINT initial_reserve_request_details_fkey FOREIGN KEY (initial_reserve_request_details) REFERENCES nsi_v2_request_details (id),
    ADD CONSTRAINT last_reservation_request_details_fkey FOREIGN KEY (last_reservation_request_details) REFERENCES nsi_v2_request_details (id),
    ADD CONSTRAINT last_lifecycle_request_details_fkey FOREIGN KEY (last_lifecycle_request_details) REFERENCES nsi_v2_request_details (id),
    ADD CONSTRAINT last_provision_request_details_fkey FOREIGN KEY (last_provision_request_details) REFERENCES nsi_v2_request_details (id);

DELETE FROM nsi_v1_request_details WHERE requester_nsa IS NOT NULL AND provider_nsa IS NOT NULL;

ALTER TABLE nsi_v1_request_details
    ALTER COLUMN reply_to SET DATA TYPE TEXT,
    DROP COLUMN requester_nsa,
    DROP COLUMN provider_nsa;
