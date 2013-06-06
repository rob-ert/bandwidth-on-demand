CREATE TABLE connection_v2 (
    id bigint NOT NULL,
    jpa_version integer,
    connection_id character varying(255) NOT NULL,
    reservation bigint,
    provider_nsa character varying(255) NOT NULL,
    requester_nsa character varying(255) NOT NULL,
    global_reservation_id character varying(255) NOT NULL,
    description character varying(255),
    desired_bandwidth integer NOT NULL,
    start_time timestamp with time zone,
    end_time timestamp with time zone,
    source_stp_id character varying(255) NOT NULL,
    destination_stp_id character varying(255) NOT NULL,
    path text NOT NULL,
    service_attributes text NOT NULL,
    provision_request_details bigint,
    protection_type character varying(50) NOT NULL,
    reservation_state character varying(20) NOT NULL,
    lifecycle_state character varying(20),
    provision_state character varying(20),
    data_plane_active boolean,
    reserve_version integer NOT NULL,
    committed_version integer
);


ALTER TABLE ONLY connection_v2
    ADD CONSTRAINT connection_v2_connection_id_key UNIQUE (connection_id);
ALTER TABLE ONLY connection_v2
    ADD CONSTRAINT connection_v2_global_reservation_id_key UNIQUE (global_reservation_id);
ALTER TABLE ONLY connection_v2
    ADD CONSTRAINT connection_v2_pkey PRIMARY KEY (id);
ALTER TABLE ONLY connection_v2
    ADD CONSTRAINT connection_v2_provision_request_details_fkey FOREIGN KEY (provision_request_details) REFERENCES nsi_request_details(id);
ALTER TABLE ONLY connection_v2
    ADD CONSTRAINT connection_v2_reservation_fkey FOREIGN KEY (reservation) REFERENCES reservation(id);

ALTER TABLE connection RENAME TO connection_v1;

ALTER TABLE connection_v1
    DROP COLUMN nsi_version,
    DROP COLUMN type,
    DROP COLUMN reservation_state,
    DROP COLUMN lifecycle_state,
    DROP COLUMN data_plane_active,
    DROP COLUMN service_attributes,
    DROP COLUMN reserve_version,
    DROP COLUMN committed_version;
