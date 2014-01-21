ALTER TABLE nsi_v2_message ADD COLUMN result_id bigint;
ALTER TABLE nsi_v2_message ADD COLUMN connection_id varchar(255);

CREATE INDEX nsi_v2_message_connection_id_idx ON nsi_v2_message (connection_id);
