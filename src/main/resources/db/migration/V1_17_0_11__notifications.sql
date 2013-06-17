CREATE TABLE notification (
        notification TEXT,
        connectionv2 BIGINT
);

ALTER TABLE ONLY notification
    ADD CONSTRAINT not_conv2_fk FOREIGN KEY (connectionv2) REFERENCES connection_v2(id);