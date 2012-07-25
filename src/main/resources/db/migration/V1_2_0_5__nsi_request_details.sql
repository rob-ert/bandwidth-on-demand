CREATE TABLE nsi_request_details (
    id bigint NOT NULL PRIMARY KEY,
    reply_to varchar(255) NOT NULL,
    correlation_id varchar(255) NOT NULL
);

ALTER TABLE connection ADD COLUMN provision_request_details BIGINT REFERENCES nsi_request_details(id);
