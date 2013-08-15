CREATE TABLE reservation_end_point (
    id BIGINT NOT NULL PRIMARY KEY,
    virtual_port BIGINT NOT NULL);

CREATE INDEX reservation_end_point__virtual_port_fkey ON reservation_end_point (virtual_port);
ALTER TABLE reservation_end_point ADD CONSTRAINT virtual_port_fkey FOREIGN KEY (virtual_port) REFERENCES virtual_port (id);

ALTER TABLE reservation 
    DROP CONSTRAINT "fka2d543cce81161d5", -- source port fkey
    DROP CONSTRAINT "fka2d543cc34f605c2"; -- destination port fkey

CREATE FUNCTION migrate_to_reservation_end_point() RETURNS void AS $$
DECLARE
  r RECORD;
  source_ep_id BIGINT;
  destination_ep_id BIGINT;
BEGIN
  FOR r IN SELECT id, source_port, destination_port FROM reservation LOOP
    source_ep_id := nextval('hibernate_sequence');
    destination_ep_id := nextval('hibernate_sequence');
    INSERT INTO reservation_end_point (id, virtual_port) VALUES (source_ep_id, r.source_port);
    INSERT INTO reservation_end_point (id, virtual_port) VALUES (destination_ep_id, r.destination_port);
    UPDATE reservation SET source_port = source_ep_id, destination_port = destination_ep_id WHERE id = r.id;
  END LOOP;
END;
$$ LANGUAGE plpgsql;

SELECT migrate_to_reservation_end_point();

DROP FUNCTION migrate_to_reservation_end_point();

ALTER TABLE reservation 
    ADD CONSTRAINT source_port_fkey FOREIGN KEY (source_port) REFERENCES reservation_end_point (id),
    ADD CONSTRAINT destination_port_fkey FOREIGN KEY (destination_port) REFERENCES reservation_end_point (id);
