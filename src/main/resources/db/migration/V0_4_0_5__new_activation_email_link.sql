DROP TABLE  IF EXISTS activation_email_link;

CREATE TABLE activation_email_link (
    id bigint NOT NULL,
    version integer NOT NULL DEFAULT 0,
    uuid character varying(40) NOT NULL,
    email_sent_date_time TIMESTAMP,
    activation_date_time TIMESTAMP,
    request_source character varying(255) NOT NULL,    
    source_id bigint NOT NULL
);

ALTER TABLE ONLY activation_email_link
    ADD CONSTRAINT activate_email_link_pkey PRIMARY KEY (id);