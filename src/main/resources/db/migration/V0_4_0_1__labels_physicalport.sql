ALTER TABLE physical_port RENAME COLUMN name TO noc_label;
ALTER TABLE physical_port ADD CHECK (noc_label <> '');

ALTER TABLE physical_port ADD COLUMN manager_label VARCHAR(100);
ALTER TABLE physical_port ADD CHECK (manager_label <> '');

ALTER TABLE physical_port DROP COLUMN display_name;