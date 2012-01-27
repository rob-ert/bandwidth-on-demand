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

import static org.junit.Assert.*;

import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.VirtualPortFactory;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This test only works against a default OpenDRAC (with standard admin pwd)
 * with the simulator and the 6 simulated NE's or against production.
 * 
 * @author robert
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/spring/bod-opendrac-test.xml")
public class NbiServiceOpenDracTestIntegration {

  @Autowired
  private NbiService nbiService;

  @Value("${nbi.service.scheduling}")
  private String schedulingServiceUrl;

  private enum NBI_TYPE {
    OFFLINE, LOCAL, PROD;
  }

  private NBI_TYPE nbiType = NBI_TYPE.OFFLINE;

  @Before
  public void setupBeforeClass() {
    if (nbiService.getClass().isAssignableFrom(NbiServiceOffline.class)) {
      nbiType = NBI_TYPE.OFFLINE;
    }
    else if (nbiService.getClass().isAssignableFrom(NbiServiceOpenDracWs.class)) {
      if (schedulingServiceUrl.startsWith("https://localhost")) {
        nbiType = NBI_TYPE.LOCAL;
      }
      else if (schedulingServiceUrl.startsWith("https://drac.surfnet.nl:8443")) {
        nbiType = NBI_TYPE.PROD;
      }
    }
  }

  @Test
  public void testFindAllPhysicalPorts() throws Exception {
    final List<PhysicalPort> allPorts = nbiService.findAllPhysicalPorts();
    if (nbiType == NBI_TYPE.OFFLINE) {
      assertEquals(18, allPorts.size());
    }
    else if (nbiType == NBI_TYPE.LOCAL) {
      assertEquals(14, allPorts.size());
    }
    else if (nbiType == NBI_TYPE.PROD) {
      assertEquals(3, allPorts.size());
    }
  }

  @Test
  public void testFindPhysicalPortByName() throws Exception {
    if (nbiType == NBI_TYPE.OFFLINE) {
      final PhysicalPort port = nbiService.findPhysicalPortByNetworkElementId("00-1B-25-2D-DA-65_ETH-1-1-4");
      assertEquals("00-1B-25-2D-DA-65_ETH-1-1-4", port.getNetworkElementPk());
      assertEquals("Mock_Ut002A_OME01_ETH-1-1-4", port.getName());
    }
    else if (nbiType == NBI_TYPE.LOCAL) {
      final PhysicalPort port = nbiService.findPhysicalPortByNetworkElementId("00-20-D8-DF-33-59_ETH-1-1-1");
      assertEquals("00-20-D8-DF-33-59_ETH-1-1-1", port.getNetworkElementPk());
      assertEquals("Asd001A_OME3T_ETH-1-1-1", port.getName());
    }
    else if (nbiType == NBI_TYPE.PROD) {
      final PhysicalPort port = nbiService.findPhysicalPortByNetworkElementId("00-21-E1-D9-CC-70_ETH-1-36-4");
      assertEquals("00-21-E1-D9-CC-70_ETH-1-36-4", port.getNetworkElementPk());
      assertEquals("Asd001A_OME12_ETH-1-36-4", port.getName());
    }
  }

  @Test
  public void testFindPhysicalPortByNetworkElementId() throws Exception {
    if (nbiType == NBI_TYPE.OFFLINE) {
      final PhysicalPort port = nbiService.findPhysicalPortByNetworkElementId("00-20-D8-DF-33-59_ETH-1-1-1");
      assertEquals("Mock_Asd001A_OME3T_ETH-1-1-1", port.getName());
      assertEquals("00-20-D8-DF-33-59_ETH-1-1-1", port.getNetworkElementPk());
    }
    else if (nbiType == NBI_TYPE.LOCAL) {
      final PhysicalPort port = nbiService.findPhysicalPortByNetworkElementId("00-20-D8-DF-33-59_ETH-1-1-1");
      assertEquals("Asd001A_OME3T_ETH-1-1-1", port.getName());
      assertEquals("00-20-D8-DF-33-59_ETH-1-1-1", port.getNetworkElementPk());
    }
    else if (nbiType == NBI_TYPE.PROD) {
      final PhysicalPort port = nbiService.findPhysicalPortByNetworkElementId("00-21-E1-D9-CC-70_ETH-1-36-4");
      assertEquals("Asd001A_OME12_ETH-1-36-4", port.getName());
      assertEquals("00-21-E1-D9-CC-70_ETH-1-36-4", port.getNetworkElementPk());
    }
  }

  @Test
  public void testCreateReservation() throws Exception {
    final LocalTime nowTime = new LocalTime(System.currentTimeMillis());
    final LocalDate nowDate = new LocalDate(System.currentTimeMillis());

    String sourcePort = null;
    String destinationPort = null;
    if (nbiType == NBI_TYPE.OFFLINE) {
      sourcePort = "Ut002A_OME01_ETH-1-1-4";
      destinationPort = "Asd001A_OME12_ETH-1-36-4";
    }
    else if (nbiType == NBI_TYPE.LOCAL) {
      sourcePort = "Asd001A_OME3T_ETH-1-1-3";
      destinationPort = "Asd001A_OME1T_ETH-1-2-1";
    }
    else if (nbiType == NBI_TYPE.PROD) {
      sourcePort = "Ut002A_OME01_ETH-1-1-4";
      destinationPort = "Asd001A_OME12_ETH-1-36-4";
    }

    final PhysicalPort physicalPort1 = new PhysicalPortFactory().setName(sourcePort).create();

    final PhysicalPort physicalPort2 = new PhysicalPortFactory().setName(destinationPort).create();

    final VirtualPort source = new VirtualPortFactory().setManagerLabel("vp1").setPhysicalPort(physicalPort1).create();
    final VirtualPort destination = new VirtualPortFactory().setManagerLabel("vp2").setPhysicalPort(physicalPort2)
        .create();

    final Reservation reservation = new ReservationFactory().setStartTime(nowTime.plusMinutes(1))
        .setEndTime(nowTime.plusMinutes(20)).setStartDate(nowDate).setEndDate(nowDate.plusYears(0))
        .setSourcePort(source).setDestinationPort(destination).setBandwidth(100).create();

    final String reservationId = nbiService.createReservation(reservation);
    assertNotNull(reservationId);
    ReservationStatus status = nbiService.getReservationStatus(reservationId);
    assertEquals(ReservationStatus.SCHEDULED, status);
    nbiService.cancelReservation(reservationId);
    status = nbiService.getReservationStatus(reservationId);
    assertEquals(ReservationStatus.CANCELLED, status);
  }

}
