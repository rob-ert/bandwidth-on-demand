CREATE TABLE "connection"
(
  id bigint NOT NULL,
  connection_id character varying(255) NOT NULL,
  current_state character varying(50) NOT NULL,
  description character varying(255) NOT NULL,
  desired_bandwidth integer NOT NULL,
  end_time timestamp without time zone NOT NULL,
  global_reservation_id character varying(255) NOT NULL,
  maximum_bandwidth integer NOT NULL,
  minimum_bandwidth integer NOT NULL,
  path bytea NOT NULL,
  provider_nsa character varying(255) NOT NULL,
  reply_to character varying(255) NOT NULL,
  requester_nsa character varying(255) NOT NULL,
  reservation_id character varying(255),
  service_parameters bytea NOT NULL,
  start_time timestamp without time zone NOT NULL,
  "version" integer,
  CONSTRAINT connection_pkey PRIMARY KEY (id),
  CONSTRAINT connection_connection_id_key UNIQUE (connection_id),
  CONSTRAINT connection_global_reservation_id_key UNIQUE (global_reservation_id)
)
WITH (
  OIDS=FALSE
);
