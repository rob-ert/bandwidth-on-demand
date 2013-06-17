CREATE TABLE notification (
        id BIGINT NOT NULL,
        notification TEXT,
        connectionv2 BIGINT,
    CONSTRAINT notification_pkey PRIMARY KEY (id)
);

ALTER TABLE ONLY notification
    ADD CONSTRAINT not_conv2_fk FOREIGN KEY (connectionv2) REFERENCES connection_v2(id);