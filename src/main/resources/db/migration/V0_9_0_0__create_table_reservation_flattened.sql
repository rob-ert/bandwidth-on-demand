-- Table: reservation_flattened

CREATE TABLE reservation_flattened
(
  id bigint NOT NULL,
  reservation_as_string character varying(255),
  "version" integer,
  CONSTRAINT reservation_flattened_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
