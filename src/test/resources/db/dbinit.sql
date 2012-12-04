--
-- Copyright (c) 2012, SURFnet BV
-- All rights reserved.
--
-- Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
--
--   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
--   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
--   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
--
-- THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
--

insert into physical_resource_group (id, admin_group, institute_id, version) values (1, 'urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:bandwidth-on-demand',564, 0);
insert into physical_resource_group (id, admin_group, institute_id, version) values (2, 'urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:ict-managers', 691,  0);

insert into physical_port (id, display_name, name, physical_resource_group, version, network_element_pk) values (1, 'ETH-1-13-4', '00-21-E1-D6-D6-70_ETH-1-13-4', 1, 0, '00-1B-25-2D-DA-65_ETH-1-1-4');
insert into physical_port (id, display_name, name, physical_resource_group, version, network_element_pk) values (2, 'ETH10G-1-13-1', '00-21-E1-D6-D6-70_ETH10G-1-13-1', 1, 0, '00-1B-25-2D-DA-65_ETH-1-1-5');
insert into physical_port (id, display_name, name, physical_resource_group, version, network_element_pk) values (3, 'ETH10G-1-13-2', '00-21-E1-D6-D6-70_ETH10G-1-13-2', 1, 0, '00-1B-25-2D-DA-65_ETH-1-1-6');
insert into physical_port (id, display_name, name, physical_resource_group, version, network_element_pk) values (4, 'ETH-1-13-4', '00-21-E1-D6-D5-DC_ETH-1-13-4', 1, 0, '00-1B-25-2D-DA-65_ETH-1-1-7');

insert into physical_port (id, display_name, name, physical_resource_group, version, network_element_pk) values (5, 'ETH10G-1-13-1', '00-21-E1-D6-D5-DC_ETH10G-1-13-1', 2, 0, '00-1B-25-2D-DA-65_ETH-1-1-8');
insert into physical_port (id, display_name, name, physical_resource_group, version, network_element_pk) values (6, 'ETH10G-1-13-2', '00-21-E1-D6-D5-DC_ETH10G-1-13-2', 2, 0, '00-1B-25-2D-DA-65_ETH-1-1-9');

insert into virtual_resource_group (id, name, surf_conext_group_name, version) values (1, 'vrg1', 'urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:institution-users', 0);
insert into virtual_resource_group (id, name, surf_conext_group_name, version) values (2, 'vrg2', 'urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:institution-users2', 0);

insert into virtual_port (id, manager_label, max_bandwidth, physical_port, version, virtual_resource_group) values (1, 'vp1-1', 10000, 1, 0, 1);
insert into virtual_port (id, manager_label, max_bandwidth, physical_port, version, virtual_resource_group) values (2, 'vp2-1', 8000, 2, 0, 1);
insert into virtual_port (id, manager_label, max_bandwidth, physical_port, version, virtual_resource_group) values (3, 'vp3-2', 3000, 3, 0, 2);
insert into virtual_port (id, manager_label, max_bandwidth, physical_port, version, virtual_resource_group) values (4, 'vp4-2', 1000, 4, 0, 2);
