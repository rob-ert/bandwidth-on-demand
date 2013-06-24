UPDATE log_event SET old_reservation_status = 'PASSED_END_TIME' where old_reservation_status = 'TIMED_OUT';
UPDATE log_event SET new_reservation_status = 'PASSED_END_TIME' where new_reservation_status = 'TIMED_OUT';
