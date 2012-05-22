-- Table: reservation_flattened

DROP TABLE reservation_flattened;

CREATE TABLE reservation_flattened
(
  id bigint NOT NULL,
  bandwidth integer NOT NULL,
  creation_date_time timestamp without time zone NOT NULL,
  destination_manager_label character varying(255) NOT NULL,
  destination_max_bandwidth integer NOT NULL,
  destination_user_label character varying(255),
  destination_vlan_id integer,
  end_date_time timestamp without time zone,
  failed_message character varying(255),
  "name" character varying(255),
  pp_destination_manager_label character varying(255),
  pp_destination_network_element_pk character varying(255) NOT NULL,
  pp_destination_noc_label character varying(255) NOT NULL,
  pp_destination_port_id character varying(255) NOT NULL,
  pp_source_manager_label character varying(255),
  pp_source_network_element_pk character varying(255) NOT NULL,
  pp_source_noc_label character varying(255) NOT NULL,
  pp_source_port_id character varying(255) NOT NULL,
  reservation_id character varying(255),
  source_manager_label character varying(255) NOT NULL,
  source_max_bandwidth integer NOT NULL,
  source_user_label character varying(255),
  source_vlan_id integer,
  start_date_time timestamp without time zone,
  status character varying(255),
  user_created character varying(255) NOT NULL,
  "version" integer,
  virtual_resource_group_description character varying(255),
  virtual_resource_group_name character varying(255) NOT NULL,
  virtual_resource_group_surfconext_group_id character varying(255) NOT NULL,
  CONSTRAINT reservation_flattened_pkey PRIMARY KEY (id),
  CONSTRAINT reservation_flattened_destination_manager_label_key UNIQUE (destination_manager_label),
  CONSTRAINT reservation_flattened_destination_user_label_key UNIQUE (destination_user_label),
  CONSTRAINT reservation_flattened_pp_destination_network_element_pk_key UNIQUE (pp_destination_network_element_pk),
  CONSTRAINT reservation_flattened_pp_source_network_element_pk_key UNIQUE (pp_source_network_element_pk),
  CONSTRAINT reservation_flattened_source_manager_label_key UNIQUE (source_manager_label),
  CONSTRAINT reservation_flattened_source_user_label_key UNIQUE (source_user_label),
  CONSTRAINT reservation_flattened_virtual_resource_group_surfconext_gro_key UNIQUE (virtual_resource_group_surfconext_group_id),
  CONSTRAINT reservation_flattened_destination_vlan_id_check CHECK (destination_vlan_id <= 4095 AND destination_vlan_id >= 1),
  CONSTRAINT reservation_flattened_source_vlan_id_check CHECK (source_vlan_id >= 1 AND source_vlan_id <= 4095)
)
WITH (
  OIDS=FALSE
);
