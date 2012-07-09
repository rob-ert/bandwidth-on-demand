ALTER TABLE connection DROP COLUMN reservation_id;

ALTER TABLE connection ADD COLUMN reservation bigint;