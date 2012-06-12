ALTER TABLE reservation RENAME COLUMN failed_message TO failed_reason;
ALTER TABLE reservation_archive RENAME COLUMN failed_message TO failed_reason;