ALTER TABLE virtual_port_request_link add column requestor_email varchar(100);
ALTER TABLE virtual_port_request_link add column requestor_name varchar(100);
ALTER TABLE virtual_port_request_link rename column requestor to requestor_urn;

UPDATE virtual_port_request_link set requestor_email = 'bod-dev@list.surfnet.nl' where requestor_email is null;
UPDATE virtual_port_request_link link set requestor_name = 'User ' || split_part((select requestor_urn from virtual_port_request_link where id = link.id), ':', 5) where requestor_name is null;

ALTER TABLE virtual_port_request_link ALTER COLUMN requestor_email SET NOT NULL;
ALTER TABLE virtual_port_request_link ALTER COLUMN requestor_name SET NOT NULL;