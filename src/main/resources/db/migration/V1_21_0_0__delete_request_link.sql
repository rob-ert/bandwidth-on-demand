ALTER TABLE virtual_port_request_link RENAME TO virtual_port_create_request_link;
ALTER INDEX virtual_port_request_link_pkey RENAME TO virtual_port_create_request_link_pkey;
ALTER TABLE virtual_port_create_request_link
  DROP CONSTRAINT vprl_prg_fk;
ALTER TABLE virtual_port_create_request_link
  ADD CONSTRAINT vpcrl_prg_fk FOREIGN KEY (physical_resource_group) REFERENCES physical_resource_group(id);
ALTER TABLE virtual_port_create_request_link
  DROP CONSTRAINT vprl_vrg_fk;
ALTER TABLE virtual_port_create_request_link
  ADD CONSTRAINT vpcrl_vrg_fk FOREIGN KEY (virtual_resource_group) REFERENCES virtual_resource_group(id);

CREATE TABLE virtual_port_delete_request_link (
  id BIGINT NOT NULL,
  version INTEGER NOT NULL DEFAULT 0,
  uuid VARCHAR(40) NOT NULL,
  request_date_time TIMESTAMP,
  message VARCHAR(255),
  status VARCHAR(100) NOT NULL,
  requestor_urn VARCHAR(255) NOT NULL,
  requestor_email VARCHAR(100) NOT NULL,
  requestor_name VARCHAR(100) NOT NULL,
  virtual_port_label VARCHAR(255) NOT NULL,
  physical_resource_group BIGINT NOT NULL,
  virtual_resource_group BIGINT NOT NULL,
  virtual_port BIGINT
);
ALTER TABLE virtual_port_delete_request_link
  ADD CONSTRAINT virtual_port_delete_request_link_pkey PRIMARY KEY (id);
ALTER TABLE virtual_port_delete_request_link
  ADD CONSTRAINT vpdrl_vp_fk FOREIGN KEY (virtual_port) REFERENCES virtual_port(id);
ALTER TABLE virtual_port_delete_request_link
  ADD CONSTRAINT vpdrl_prg_fk FOREIGN KEY (physical_resource_group) REFERENCES physical_resource_group(id);
ALTER TABLE virtual_port_delete_request_link
  ADD CONSTRAINT vpdrl_vrg_fk FOREIGN KEY (virtual_resource_group) REFERENCES virtual_resource_group(id);
