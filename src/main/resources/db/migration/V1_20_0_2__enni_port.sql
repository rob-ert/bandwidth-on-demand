ALTER TABLE physical_port ADD COLUMN inbound_peer varchar(255);
ALTER TABLE physical_port ADD COLUMN outbound_peer varchar(255);
ALTER TABLE physical_port ADD COLUMN vlan_ranges varchar(255);
ALTER TABLE physical_port ADD COLUMN interface_type varchar(200);

UPDATE physical_port set interface_type = 'UNI';
