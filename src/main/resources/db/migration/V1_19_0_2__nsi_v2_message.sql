CREATE TABLE nsi_v2_message (
    id BIGINT NOT NULL PRIMARY KEY,
    requester_nsa character varying(255) NOT NULL,
    correlation_id character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    soap_action TEXT,
    message TEXT NOT NULL);

CREATE UNIQUE INDEX nsi_v2_message_requester_nsa_correlation_id_type_idx ON nsi_v2_message (requester_nsa, correlation_id, type);
