INSERT INTO activation_email_link (id, activation_date_time, email_sent_date_time, request_source, source_id, to_email, uuid, version) VALUES (6, '2012-06-19 14:53:18.041', '2012-06-19 14:52:41.753', 'PHYSICAL_RESOURCE_GROUP', 5, 'me@me.com', '984ba1a5-5648-4aea-9d22-bfd108f19bd0', 2);
INSERT INTO activation_email_link (id, activation_date_time, email_sent_date_time, request_source, source_id, to_email, uuid, version) VALUES (4, '2012-06-19 14:53:44.202', '2012-06-19 14:52:04.935', 'PHYSICAL_RESOURCE_GROUP', 3, 'i@i.com', 'c35e3aef-3611-47c2-95fe-c7691bb32a8b', 2);

INSERT INTO institute (id, name, short_name, aligned_idd) VALUES (1, 'Mock_Klantnaam', 'Mock_klantafkorting', true);
INSERT INTO institute (id, name, short_name, aligned_idd) VALUES (564, '2COLLEGE', '2COLLEGE', true);
INSERT INTO institute (id, name, short_name, aligned_idd) VALUES (620, 'De Kempel', 'KEMPEL', true);
INSERT INTO institute (id, name, short_name, aligned_idd) VALUES (689, 'Deltion College', 'DELTION', true);
INSERT INTO institute (id, name, short_name, aligned_idd) VALUES (449, 'Design Academy', 'DESIGNACADEMY', true);
INSERT INTO institute (id, name, short_name, aligned_idd) VALUES (480, 'Dienst Uitvoering Onderwijs (DUO, voorheen IB-groep)', 'IB-GROEP', true);

INSERT INTO physical_resource_group (id, active, admin_group, institute_id, manager_email, version) VALUES (5, true, 'urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:ict-uu', 564, 'me@me.com', 1);
INSERT INTO physical_resource_group (id, active, admin_group, institute_id, manager_email, version) VALUES (3, true, 'urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:ict-uu', 620, 'i@i.com', 1);

INSERT INTO physical_port (id, manager_label, nms_port_id, noc_label, bod_port_id, version, vlan_required, physical_resource_group, aligned_nms, nms_ne_id, nms_port_speed, nms_sap_name, signaling_type, supported_service_type) VALUES (8, 'UT One', '00-20-D8-DF-33-86_ETH-1-13-1', 'Mock_Ut002A_OME01_ETH-1-2-4', 'Mock_Ut002A_OME01_ETH-1-2-4', 2, false, 5, true, NULL, NULL, NULL, NULL, NULL);
INSERT INTO physical_port (id, manager_label, nms_port_id, noc_label, bod_port_id, version, vlan_required, physical_resource_group, aligned_nms, nms_ne_id, nms_port_speed, nms_sap_name, signaling_type, supported_service_type) VALUES (9, 'UT Two', '00-02-F8-EF-35-96_ETH-1-15-1', 'Mock_Ut001A_OME01_ETH-1-2-1', 'Mock_Ut001A_OME01_ETH-1-2-1', 2, false, 5, true, NULL, NULL, NULL, NULL, NULL);