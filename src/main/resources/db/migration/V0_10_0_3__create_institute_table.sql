CREATE TABLE institute
(
  id bigint NOT NULL,
  name character varying(255) NOT NULL,
  short_name character varying(255) NOT NULL,
  version integer,
  CONSTRAINT institute_key PRIMARY KEY (id)
);