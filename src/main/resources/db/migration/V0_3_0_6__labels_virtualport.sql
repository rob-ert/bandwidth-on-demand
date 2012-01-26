ALTER TABLE virtual_port RENAME COLUMN name TO manager_label;
ALTER TABLE virtual_port ADD CHECK (manager_label <> '');
ALTER TABLE virtual_port ADD COLUMN user_label VARCHAR(255) UNIQUE;
