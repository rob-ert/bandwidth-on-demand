SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET search_path = public, pg_catalog;

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

SET default_tablespace = '';
SET default_with_oids = false;

CREATE TABLE physical_port (
    id bigint NOT NULL,
    display_name character varying(255),
    name character varying(255) NOT NULL,
    version integer,
    physical_resource_group bigint
);

CREATE TABLE physical_resource_group (
    id bigint NOT NULL,
    admin_group character varying(255),
    institution_name character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    version integer
);

CREATE TABLE reservation (
    id bigint NOT NULL,
    end_date bytea NOT NULL,
    end_time bytea NOT NULL,
    start_date bytea NOT NULL,
    start_time bytea NOT NULL,
    status character varying(255),
    user_created character varying(255) NOT NULL,
    version integer,
    destination_port bigint NOT NULL,
    source_port bigint NOT NULL,
    virtual_resource_group bigint
);

CREATE TABLE virtual_port (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    version integer,
    physical_port bigint,
    virtual_resource_group bigint
);

CREATE TABLE virtual_resource_group (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    surf_conext_group_name character varying(255) NOT NULL,
    version integer
);

ALTER TABLE ONLY physical_port
    ADD CONSTRAINT physical_port_name_key UNIQUE (name);

ALTER TABLE ONLY physical_port
    ADD CONSTRAINT physical_port_pkey PRIMARY KEY (id);

ALTER TABLE ONLY physical_resource_group
    ADD CONSTRAINT physical_resource_group_institution_name_key UNIQUE (institution_name);

ALTER TABLE ONLY physical_resource_group
    ADD CONSTRAINT physical_resource_group_name_key UNIQUE (name);

ALTER TABLE ONLY physical_resource_group
    ADD CONSTRAINT physical_resource_group_pkey PRIMARY KEY (id);

ALTER TABLE ONLY reservation
    ADD CONSTRAINT reservation_pkey PRIMARY KEY (id);

ALTER TABLE ONLY virtual_port
    ADD CONSTRAINT virtual_port_name_key UNIQUE (name);

ALTER TABLE ONLY virtual_port
    ADD CONSTRAINT virtual_port_pkey PRIMARY KEY (id);

ALTER TABLE ONLY virtual_resource_group
    ADD CONSTRAINT virtual_resource_group_name_key UNIQUE (name);

ALTER TABLE ONLY virtual_resource_group
    ADD CONSTRAINT virtual_resource_group_pkey PRIMARY KEY (id);

ALTER TABLE ONLY virtual_resource_group
    ADD CONSTRAINT virtual_resource_group_surf_conext_group_name_key UNIQUE (surf_conext_group_name);

ALTER TABLE ONLY physical_port
    ADD CONSTRAINT fk5bb15ee93ce65b74 FOREIGN KEY (physical_resource_group) REFERENCES physical_resource_group(id);

ALTER TABLE ONLY reservation
    ADD CONSTRAINT fka2d543cc34f605c2 FOREIGN KEY (destination_port) REFERENCES virtual_port(id);

ALTER TABLE ONLY reservation
    ADD CONSTRAINT fka2d543cc8cfd8b84 FOREIGN KEY (virtual_resource_group) REFERENCES virtual_resource_group(id);

ALTER TABLE ONLY reservation
    ADD CONSTRAINT fka2d543cce81161d5 FOREIGN KEY (source_port) REFERENCES virtual_port(id);

ALTER TABLE ONLY virtual_port
    ADD CONSTRAINT fkd846e1358cfd8b84 FOREIGN KEY (virtual_resource_group) REFERENCES virtual_resource_group(id);

ALTER TABLE ONLY virtual_port
    ADD CONSTRAINT fkd846e135e30b47dd FOREIGN KEY (physical_port) REFERENCES physical_port(id);