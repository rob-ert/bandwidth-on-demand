CREATE TABLE activation_email_link (
    id bigint NOT NULL,
    version integer NOT NULL DEFAULT 0,
    uuid character varying(40) NOT NULL,
    creation_date_time TIMESTAMP,
    physical_resource_group bigint
);

ALTER TABLE ONLY activation_email_link
    ADD CONSTRAINT activate_email_link_pkey PRIMARY KEY (id);
    
ALTER TABLE ONLY activation_email_link
    ADD CONSTRAINT ael_prg_fk FOREIGN KEY (physical_resource_group) REFERENCES physical_resource_group(id);
    
ALTER TABLE physical_resource_group ADD COLUMN active BOOLEAN NOT NULL DEFAULT FALSE;