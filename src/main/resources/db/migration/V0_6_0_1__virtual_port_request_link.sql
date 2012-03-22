CREATE TABLE virtual_port_request_link (
    id bigint NOT NULL,
    version integer NOT NULL DEFAULT 0,
    uuid character varying(40) NOT NULL,
    requestor character varying(255) NOT NULL,
    request_date_time TIMESTAMP,
    virtual_resource_group bigint NOT NULL,
    physical_resource_group bigint NOT NULL,
    message character varying(255),
    min_bandwidth integer
);
ALTER TABLE ONLY virtual_port_request_link
    ADD CONSTRAINT virtual_port_request_link_pkey PRIMARY KEY (id);  
ALTER TABLE ONLY virtual_port_request_link
    ADD CONSTRAINT vprl_prg_fk FOREIGN KEY (physical_resource_group) REFERENCES physical_resource_group(id);
ALTER TABLE ONLY virtual_port_request_link
    ADD CONSTRAINT vprl_vrg_fk FOREIGN KEY (virtual_resource_group) REFERENCES virtual_resource_group(id);