--
-- Copyright (c) 2012, 2013 SURFnet BV
-- All rights reserved.
--
-- Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
-- following conditions are met:
--
--   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
--     disclaimer.
--   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
--     disclaimer in the documentation and/or other materials provided with the distribution.
--   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
--     derived from this software without specific prior written permission.
--
-- THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
-- INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
-- DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
-- SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
-- SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
-- WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
-- THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
--

INSERT INTO activation_email_link (id, activation_date_time, email_sent_date_time, request_source, source_id, to_email, uuid, version) VALUES (6, '2012-06-19 14:53:18.041', '2012-06-19 14:52:41.753', 'PHYSICAL_RESOURCE_GROUP', 5, 'me@me.com', '984ba1a5-5648-4aea-9d22-bfd108f19bd0', 2);
INSERT INTO activation_email_link (id, activation_date_time, email_sent_date_time, request_source, source_id, to_email, uuid, version) VALUES (4, '2012-06-19 14:53:44.202', '2012-06-19 14:52:04.935', 'PHYSICAL_RESOURCE_GROUP', 3, 'i@i.com', 'c35e3aef-3611-47c2-95fe-c7691bb32a8b', 2);

INSERT INTO institute (id, name, short_name, aligned_idd) VALUES (100, 'Mock_Klantnaam', 'Mock_klantafkorting', true);
INSERT INTO institute (id, name, short_name, aligned_idd) VALUES (564, '2COLLEGE', '2COLLEGE', true);
INSERT INTO institute (id, name, short_name, aligned_idd) VALUES (620, 'De Kempel', 'KEMPEL', true);
INSERT INTO institute (id, name, short_name, aligned_idd) VALUES (689, 'Deltion College', 'DELTION', true);
INSERT INTO institute (id, name, short_name, aligned_idd) VALUES (449, 'Design Academy', 'DESIGNACADEMY', true);
INSERT INTO institute (id, name, short_name, aligned_idd) VALUES (480, 'Dienst Uitvoering Onderwijs (DUO, voorheen IB-groep)', 'IB-GROEP', true);

INSERT INTO physical_resource_group (id, active, admin_group, institute_id, manager_email, version) VALUES (5, true, 'urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:ict-uu', 564, 'me@me.com', 1);
INSERT INTO physical_resource_group (id, active, admin_group, institute_id, manager_email, version) VALUES (3, true, 'urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:ict-sara', 620, 'i@i.com', 1);

INSERT INTO physical_port (id, manager_label, nms_port_id, noc_label, bod_port_id, version, vlan_required, physical_resource_group, aligned_nms, nms_ne_id, nms_port_speed, nms_sap_name, signaling_type, supported_service_type) VALUES (1, 'UT One', '00-20-D8-DF-33-86_ETH-1-13-1', 'Mock_Ut002A_OME01_ETH-1-2-1', 'Mock_Ut002A_OME01_ETH-1-2-1', 2, false, 5, true, NULL, NULL, NULL, NULL, NULL);
INSERT INTO physical_port (id, manager_label, nms_port_id, noc_label, bod_port_id, version, vlan_required, physical_resource_group, aligned_nms, nms_ne_id, nms_port_speed, nms_sap_name, signaling_type, supported_service_type) VALUES (2, 'UT Two', '00-02-F8-EF-35-96_ETH-1-15-2', 'Mock_Ut001A_OME01_ETH-1-2-2', 'Mock_Ut001A_OME01_ETH-1-2-2', 2, false, 5, true, NULL, NULL, NULL, NULL, NULL);
INSERT INTO physical_port (id, manager_label, nms_port_id, noc_label, bod_port_id, version, vlan_required, physical_resource_group, aligned_nms, nms_ne_id, nms_port_speed, nms_sap_name, signaling_type, supported_service_type) VALUES (3, 'TU Three', '00-20-D8-DF-33-86_ETH-1-13-3', 'Noc 3 label', 'Mock_port 2de verdieping toren1b', 2, false, 5, true, NULL, NULL, NULL, NULL, NULL);
INSERT INTO physical_port (id, manager_label, nms_port_id, noc_label, bod_port_id, version, vlan_required, physical_resource_group, aligned_nms, nms_ne_id, nms_port_speed, nms_sap_name, signaling_type, supported_service_type) VALUES (4, 'TU Four', '00-20-D8-DF-33-86_ETH-1-13-4', 'Noc 4 label', 'Mock_port 1de verdieping toren1a', 2, false, 5, true, NULL, NULL, NULL, NULL, NULL);

INSERT INTO log_event (id, created, user_id, event_type, description, serialized_object, details, correlation_id) VALUES (93, '2012-07-16 13:52:56.971', 'system', 'UPDATE', 'ArrayList', '[{"id":{"groupId":"urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:noc-engineer","type":"groupId"},"title":"noc-engineer","description":"NOC engineers"}, {"id":{"groupId":"urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:ict-uu","type":"groupId"},"title":"ict-uu","description":"UU"}, {"id":{"groupId":"urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:ict-sara","type":"groupId"},"title":"ict-sara","description":"SARA"}, {"id":{"groupId":"urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:ict-managers","type":"groupId"},"title":"ict-managers","description":"ICT Managers X"}, {"id":{"groupId":"urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:users-klimaat","type":"groupId"},"title":"users-klimaat","description":"Klimaat onderzoekers"}, {"id":{"groupId":"urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:users-klimaat2","type":"groupId"},"title":"users-klimaat2","description":"Klimaat onderzoekers 2"}, {"id":{"groupId":"urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:bandwidth-on-demand","type":"groupId"},"title":"bandwidth-on-demand","description":"BoD group"}, {"id":{"groupId":"urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:institution-users","type":"groupId"},"title":"institution-users","description":"Users"}, {"id":{"groupId":"urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:institution-users2","type":"groupId"},"title":"institution-users2","description":"Users 2"}]', NULL, NULL);
INSERT INTO log_event (id, created, user_id, event_type, description, serialized_object, details, correlation_id) VALUES (292, '2012-08-15 19:20:50.436', 'system', 'DELETE', 'List of 9 Group(s)', '[{"id":{"groupId":"urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:noc-engineer","type":"groupId"},"title":"noc-engineer","description":"NOC engineers"}, {"id":{"groupId":"urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:ict-uu","type":"groupId"},"title":"ict-uu","description":"UU"}, {"id":{"groupId":"urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:ict-sara","type":"groupId"},"title":"ict-sara","description":"SARA"}, {"id":{"groupId":"urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:ict-managers","type":"groupId"},"title":"ict-managers","description":"ICT Managers X"}, {"id":{"groupId":"urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:users-klimaat","type":"groupId"},"title":"users-klimaat","description":"Klimaat onderzoekers"}, {"id":{"groupId":"urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:users-klimaat1","type":"groupId"},"title":"users-klimaat1","description":"Klimaat onderzoekers 2"}, {"id":{"groupId":"urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:bandwidth-on-demand","type":"groupId"},"title":"bandwidth-on-demand","description":"BoD group"}, {"id":{"groupId":"urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:institution-users","type":"groupId"},"title":"institution-users","description":"Users"}, {"id":{"groupId":"urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:institution-users2","type":"groupId"},"title":"institution-users2","description":"Users 2"}]', NULL, NULL);
INSERT INTO log_event (id, created, user_id, event_type, description, serialized_object, details, correlation_id) VALUES (282, '2012-08-13 14:00:09.109', 'system', 'CREATE', 'Empty list', '[]', NULL, NULL);

INSERT INTO log_event_admin_groups (log_event, admin_group) VALUES (93, 'urn:surfguest:ict-managers');
INSERT INTO log_event_admin_groups (log_event, admin_group) VALUES (292, 'urn:surfguest:oneusers');
INSERT INTO log_event_admin_groups (log_event, admin_group) VALUES (292, 'urn:surfguest:twousers');