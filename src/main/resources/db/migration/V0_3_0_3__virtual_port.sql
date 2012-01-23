ALTER TABLE virtual_port ADD COLUMN max_bandwidth INTEGER NOT NULL;
ALTER TABLE virtual_port ADD COLUMN vlan_id INTEGER;
ALTER TABLE virtual_port ALTER COLUMN virtual_resource_group SET NOT NULL;
ALTER TABLE virtual_port ALTER COLUMN physical_port SET NOT NULL;