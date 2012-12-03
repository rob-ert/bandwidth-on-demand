CREATE TABLE log_event_admin_groups (
  log_event BIGINT,
  admin_group VARCHAR(255),
  FOREIGN KEY (log_event) REFERENCES log_event(id)
);

ALTER TABLE virtual_resource_group rename surfconext_group_id to admin_group;