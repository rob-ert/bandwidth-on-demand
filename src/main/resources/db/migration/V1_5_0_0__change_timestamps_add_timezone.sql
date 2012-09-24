-- activation_email_link
alter table activation_email_link alter email_sent_date_time TYPE timestamp with time zone;
alter table activation_email_link alter activation_date_time TYPE timestamp with time zone;

-- reservation
alter table reservation alter start_date_time TYPE timestamp with time zone;
alter table reservation alter end_date_time TYPE timestamp with time zone;
alter table reservation alter creation_date_time TYPE timestamp with time zone;

-- connection
alter table connection alter start_time TYPE timestamp with time zone;
alter table connection alter end_time TYPE timestamp with time zone;

-- logevent
alter table log_event alter created TYPE timestamp with time zone; 

--reservation_archive
alter table reservation_archive alter creation_date_time TYPE timestamp with time zone;
alter table reservation_archive alter end_date_time TYPE timestamp with time zone;
alter table reservation_archive alter start_date_time TYPE timestamp with time zone;

-- virtual_port_request_link
alter table virtual_port_request_link alter request_date_time TYPE timestamp with time zone;