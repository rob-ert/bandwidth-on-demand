ALTER TABLE reservation_end_point
  ALTER COLUMN virtual_port DROP NOT NULL,
  ADD COLUMN enni_port BIGINT,
  ADD COLUMN enni_vlan_id INTEGER,
  ADD CONSTRAINT virtual_xor_enni_port_required CHECK ((virtual_port IS NOT NULL AND enni_port IS NULL) OR (virtual_port IS NULL AND enni_port IS NOT NULL)),
  ADD CONSTRAINT enni_port_may_have_vlan_id CHECK ((virtual_port IS NOT NULL AND enni_vlan_id IS NULL) OR enni_port IS NOT NULL);

CREATE INDEX reservation_end_point__enni_port_fkey ON reservation_end_point (enni_port);
ALTER TABLE reservation_end_point
  ADD CONSTRAINT enni_port_fkey FOREIGN KEY (enni_port) REFERENCES physical_port (id);
