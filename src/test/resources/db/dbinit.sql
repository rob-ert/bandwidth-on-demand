--
-- The owner of the original code is SURFnet BV.
--
-- Portions created by the original owner are Copyright (C) 2011-2012 the
-- original owner. All Rights Reserved.
--
-- Portions created by other contributors are Copyright (C) the contributor.
-- All Rights Reserved.
--
-- Contributor(s):
--   (Contributors insert name & email here)
--
-- This file is part of the SURFnet7 Bandwidth on Demand software.
--
-- The SURFnet7 Bandwidth on Demand software is free software: you can
-- redistribute it and/or modify it under the terms of the BSD license
-- included with this distribution.
--
-- If the BSD license cannot be found with this distribution, it is available
-- at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
--

insert into physical_resource_group (id, admin_group, institute_id, version) values (1, 'urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:bandwidth-on-demand',564, 0);
insert into physical_resource_group (id, admin_group, institute_id, version) values (2, 'urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:ict-managers', 691,  0);

insert into physical_port (id, display_name, name, physical_resource_group, version) values (1, 'ETH-1-13-4', '00-21-E1-D6-D6-70_ETH-1-13-4', 1, 0);
insert into physical_port (id, display_name, name, physical_resource_group, version) values (2, 'ETH10G-1-13-1', '00-21-E1-D6-D6-70_ETH10G-1-13-1', 1, 0);
insert into physical_port (id, display_name, name, physical_resource_group, version) values (3, 'ETH10G-1-13-2', '00-21-E1-D6-D6-70_ETH10G-1-13-2', 1, 0);
insert into physical_port (id, display_name, name, physical_resource_group, version) values (4, 'ETH-1-13-4', '00-21-E1-D6-D5-DC_ETH-1-13-4', 1, 0);

insert into physical_port (id, display_name, name, physical_resource_group, version) values (5, 'ETH10G-1-13-1', '00-21-E1-D6-D5-DC_ETH10G-1-13-1', 2, 0);
insert into physical_port (id, display_name, name, physical_resource_group, version) values (6, 'ETH10G-1-13-2', '00-21-E1-D6-D5-DC_ETH10G-1-13-2', 2, 0);

insert into virtual_resource_group (id, name, surf_conext_group_name, version) values (1, 'vrg1', 'urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:institution-users', 0);
insert into virtual_resource_group (id, name, surf_conext_group_name, version) values (2, 'vrg2', 'urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:institution-users2', 0);

insert into virtual_port (id, name, max_bandwidth, physical_port, version, virtual_resource_group) values (1, 'vp1-1', 10000, 1, 0, 1);
insert into virtual_port (id, name, max_bandwidth, physical_port, version, virtual_resource_group) values (2, 'vp2-1', 8000, 2, 0, 1);
insert into virtual_port (id, name, max_bandwidth, physical_port, version, virtual_resource_group) values (3, 'vp3-2', 3000, 3, 0, 2);
insert into virtual_port (id, name, max_bandwidth, physical_port, version, virtual_resource_group) values (4, 'vp4-2', 1000, 4, 0, 2);
