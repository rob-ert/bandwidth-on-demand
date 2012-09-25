CREATE TABLE bod_account
(
  id BIGINT NOT NULL,
  name_id VARCHAR(255) NOT NULL,
  authorization_server_access_token VARCHAR(255),
  access_token VARCHAR(255),
  version BIGINT
);