-- Table: reservation_flattened

DROP TABLE reservation_flattened;

CREATE TABLE reservation_flattened
(
  id bigint NOT NULL,
  bandwidth integer NOT NULL,
  creation_date_time timestamp without time zone,
  destination_manager_label character varying(255),
  destination_max_bandwidth integer NOT NULL,
  destination_user_label character varying(255),
  destination_vlan_id integer NOT NULL,
  end_date_time timestamp without time zone,
  failed_message character varying(255),
  "name" character varying(255),
  physical_port_destination_manager_label character varying(255),
  physical_port_destination_network_element_pk character varying(255),
  physical_port_destination_noc_label character varying(255),
  physical_port_destination_port_id character varying(255),
  physical_port_source_manager_label character varying(255),
  physical_port_source_network_element_pk character varying(255),
  physical_port_source_noc_label character varying(255),
  physical_port_source_port_id character varying(255),
  physical_resource_group_destination_admin_group_name character varying(255),
  physical_resource_group_destination_institute_id bigint NOT NULL,
  physical_resource_group_destination_manager_email character varying(255),
  physical_resource_group_source_admin_group_name character varying(255),
  physical_resource_group_source_institute_id bigint NOT NULL,
  physical_resource_group_source_manager_email character varying(255),
  reservation_id character varying(255),
  source_manager_label character varying(255),
  source_max_bandwidth integer NOT NULL,
  source_user_label character varying(255),
  source_vlan_id integer NOT NULL,
  start_date_time timestamp without time zone,
  status character varying(255),
  user_created character varying(255),
  "version" integer NOT NULL,
  virtual_resource_group_description character varying(255),
  virtual_resource_group_name character varying(255),
  virtual_resource_group_surfconext_group_id character varying(255),
  CONSTRAINT reservation_flattened_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
