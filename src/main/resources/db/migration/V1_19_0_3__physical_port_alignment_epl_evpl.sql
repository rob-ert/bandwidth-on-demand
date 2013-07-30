ALTER TABLE physical_port ADD COLUMN nms_alignment_status CHARACTER VARYING(255);
UPDATE physical_port 
   SET nms_alignment_status = CASE aligned_nms
                                  WHEN false THEN 'DISAPPEARED'
                                  WHEN true  THEN 'ALIGNED'
                              END;
ALTER TABLE physical_port DROP COLUMN aligned_nms, ALTER COLUMN nms_alignment_status SET NOT NULL;
