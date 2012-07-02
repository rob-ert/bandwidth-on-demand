CREATE TABLE log_event (
        id bigint NOT NULL,
        created timestamp NOT NULL,
        user_id varchar(255) NOT NULL,
        group_ids text NOT NULL,
        event_type character varying(255)  NOT NULL,
        class_name character varying(255) ,
        serialized_object text,
        details text,

    CONSTRAINT log_event_pkey PRIMARY KEY (id)
);