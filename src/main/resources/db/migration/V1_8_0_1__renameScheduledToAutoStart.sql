UPDATE reservation set status = 'AUTO_START' where status = 'SCHEDULED';
UPDATE reservation_archive set status = 'AUTO_START' where status = 'SCHEDULED';