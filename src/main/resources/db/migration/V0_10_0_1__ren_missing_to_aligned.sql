-- Cannot rename since missing != aligned_nms
ALTER TABLE physical_port ADD COLUMN aligned_nms BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE physical_port DROP COLUMN missing;