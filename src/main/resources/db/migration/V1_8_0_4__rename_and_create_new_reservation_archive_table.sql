ALTER TABLE reservation_archive RENAME TO reservation_archive_old;

CREATE TABLE reservation_archive
(
  id bigint NOT NULL,
  reservation_as_json text NOT NULL,
  reservation_primary_key bigint NOT NULL,
  version integer,
  CONSTRAINT reservation_archive_new_pkey PRIMARY KEY (id),
  CONSTRAINT reservation_archive_new_reservation_id_key UNIQUE (reservation_primary_key)
);