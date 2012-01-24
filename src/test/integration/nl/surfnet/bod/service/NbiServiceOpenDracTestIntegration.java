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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testFindAllPhysicalPorts() throws Exception {
    final List<PhysicalPort> allPorts = nbiService.findAllPhysicalPorts();
    assertEquals(3, allPorts.size());
  }

  @Test
  public void testFindPhysicalPortByName() throws Exception {
    final PhysicalPort port = nbiService.findPhysicalPortByName("Ut002A_OME01_ETH-1-1-4");
    assertEquals("Ut002A_OME01_ETH-1-1-4", port.getName());
    System.out.println(port);
    assertEquals("00-1B-25-2D-DA-65_ETH-1-1-4", port.getNetworkElementPk());
  }

  @Test
  public void testCreateReservation() throws Exception {
    final LocalTime nowTime = new LocalTime(System.currentTimeMillis());
    final LocalDate nowDate = new LocalDate(System.currentTimeMillis());

    final PhysicalPort physicalPort1 = new PhysicalPortFactory().setName("Ut002A_OME01_ETH-1-1-4").create();
    final PhysicalPort physicalPort2 = new PhysicalPortFactory().setName("Asd001A_OME12_ETH-1-36-4").create();
    
    final VirtualPort source = new VirtualPortFactory().setName("vp1").setPhysicalPort(physicalPort1).create();
    final VirtualPort destination = new VirtualPortFactory().setName("vp2").setPhysicalPort(physicalPort2).create();

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
