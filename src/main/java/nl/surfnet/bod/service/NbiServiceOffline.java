/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class NbiServiceOffline implements NbiService {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final List<PhysicalPort> ports = new ArrayList<PhysicalPort>();

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    log.info("USING OFFLINE NBI CLIENT!");

    final Map<String, String> names = new HashMap<String, String>();
    names.put("ETH-1-13-4", "00-21-E1-D6-D6-70_ETH-1-13-4");
    names.put("ETH10G-1-13-1", "00-21-E1-D6-D6-70_ETH10G-1-13-1");
    names.put("ETH10G-1-13-2", "00-21-E1-D6-D6-70_ETH10G-1-13-2");
    names.put("ETH-1-13-4", "00-21-E1-D6-D5-DC_ETH-1-13-4");
    names.put("ETH10G-1-13-1", "00-21-E1-D6-D5-DC_ETH10G-1-13-1");
    names.put("ETH10G-1-13-2", "00-21-E1-D6-D5-DC_ETH10G-1-13-2");
    names.put("ETH10G-1-5-1", "00-20-D8-DF-33-8B_ETH10G-1-5-1");
    names.put("OME0039_OC12-1-12-1", "00-21-E1-D6-D6-70_OC12-1-12-1");
    names.put("WAN-1-4-102", "00-20-D8-DF-33-86_WAN-1-4-102");
    names.put("ETH-1-3-1", "00-21-E1-D6-D6-70_ETH-1-3-1");
    names.put("ETH-1-1-1", "00-21-E1-D6-D5-DC_ETH-1-1-1");
    names.put("ETH-1-2-3", "00-20-D8-DF-33-8B_ETH-1-2-3");
    names.put("WAN-1-4-101", "00-20-D8-DF-33-86_WAN-1-4-101");
    names.put("ETH-1-1-2", "00-21-E1-D6-D5-DC_ETH-1-1-2");
    names.put("OME0039_OC12-1-12-2", "00-21-E1-D6-D6-70_OC12-1-12-2");
    names.put("ETH-1-13-5", "00-21-E1-D6-D5-DC_ETH-1-13-5");
    names.put("ETH10G-1-13-3", "00-21-E1-D6-D5-DC_ETH10G-1-13-3");

    for (final Entry<String, String> entry : names.entrySet()) {
      final PhysicalPort physicalPort = new PhysicalPort();
      physicalPort.setDisplayName(entry.getKey());
      physicalPort.setName(entry.getValue());
      ports.add(physicalPort);
    }

  }

  @Override
  public List<PhysicalPort> findAllPhysicalPorts() {
    return ports;
  }

  @Override
  public long getPhysicalPortsCount() {
    return ports.size();
  }

  @Override
  public PhysicalPort findPhysicalPortByName(String name) {
    for (final PhysicalPort port : ports) {
      if (port.getName().equals(name)) {
        return port;
      }
    }
    return null;
  }

  @Override
  public String createReservation(Reservation reservation) {
    return "SCHEDULE-" + System.currentTimeMillis();
  }

  @Override
  public String getReservationStatus(String scheduleId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void cancelReservation(String scheduleId) {
    // TODO Auto-generated method stub
  }

  @Override
  public void extendReservation(String scheduleId, int minutes) {
    // TODO Auto-generated method stub

  }
}
