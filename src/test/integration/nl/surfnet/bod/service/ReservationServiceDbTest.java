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

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.repo.InstituteRepo;
import nl.surfnet.bod.repo.PhysicalPortRepo;
import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
import nl.surfnet.bod.repo.ReservationRepo;
import nl.surfnet.bod.repo.VirtualPortRepo;
import nl.surfnet.bod.repo.VirtualResourceGroupRepo;
import nl.surfnet.bod.support.ReservationFactory;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/appCtx.xml", "/spring/appCtx-jpa-test.xml",
    "/spring/appCtx-nbi-client.xml", "/spring/appCtx-idd-client.xml" })
@Transactional
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class ReservationServiceDbTest {

  // override bod.properties to run test and bod server at the same time
  static {
    System.setProperty("snmp.host", "localhost/1622");
  }

  @Resource
  private ReservationService reservationService;

  @Resource
  private ReservationRepo reservationRepo;

  @Resource
  private VirtualResourceGroupRepo virtualResourceGroupRepo;

  @Resource
  private VirtualPortRepo virtualPortRepo;

  @Resource
  private PhysicalPortRepo physicalPortRepo;

  @Resource
  private InstituteRepo instituteRepo;

  @Resource
  private PhysicalResourceGroupRepo physicalResourceGroupRepo;

  private final DateTime rightDateTime = DateTime.now().withTime(0, 0, 0, 0);
  private final DateTime beforeDateTime = rightDateTime.minusMinutes(1);

  private Reservation rightReservationOnStartTime;
  private Reservation rightReservationOnEndTime;

  @Before
  public void setUp() {
    rightReservationOnStartTime = createAndPersist(rightDateTime, beforeDateTime, ReservationStatus.AUTO_START);
    rightReservationOnEndTime = createAndPersist(beforeDateTime, rightDateTime, ReservationStatus.AUTO_START);
    createAndPersist(beforeDateTime.minusMinutes(10), rightDateTime.plusHours(1), ReservationStatus.AUTO_START);
    createAndPersist(rightDateTime, beforeDateTime, ReservationStatus.CANCELLED);
    createAndPersist(rightDateTime, rightDateTime, ReservationStatus.CANCELLED);
    createAndPersist(beforeDateTime, beforeDateTime, ReservationStatus.AUTO_START);
  }

  @Test
  public void shouldFindAll() {
    List<Reservation> allReservations = reservationRepo.findAll();

    assertThat(allReservations, hasSize(6));
  }

  @Test
  public void shouldFindScheduledReservations() {
    Collection<Reservation> reservations = reservationService.findReservationsToPoll(DateTime.now().withHourOfDay(1));

    assertThat(reservations, hasSize(4));
  }

  @Test
  public void shouldFindReservationsAfterMidnight() {
    DateTimeUtils.setCurrentMillisFixed(DateMidnight.now().getMillis());
    Collection<Reservation> reservations = reservationService.findReservationsToPoll(rightDateTime);

    assertThat(reservations, hasSize(4));
    assertThat(reservations, hasItems(rightReservationOnEndTime, rightReservationOnStartTime));
  }

  private Reservation createAndPersist(DateTime startDateTime, DateTime endDateTime, ReservationStatus status) {
    Reservation reservation = new ReservationFactory().setStartDateTime(startDateTime).setEndDateTime(endDateTime)
        .setStatus(status).create();

    // Force save of vrg only once, since they all use the same reference
    reservation.getVirtualResourceGroup().setId(null);

    persistReservation(reservation);

    return reservation;
  }

  private void persistReservation(Reservation reservation) {
    // Source port stuff
    reservation.getSourcePort().getPhysicalResourceGroup().setId(null);
    instituteRepo.save(reservation.getSourcePort().getPhysicalResourceGroup().getInstitute());
    physicalResourceGroupRepo.save(reservation.getSourcePort().getPhysicalPort().getPhysicalResourceGroup());

    reservation.getSourcePort().getPhysicalPort().setId(null);
    physicalPortRepo.save(reservation.getSourcePort().getPhysicalPort());

    virtualResourceGroupRepo.save(reservation.getSourcePort().getVirtualResourceGroup());

    reservation.getSourcePort().setId(null);
    virtualPortRepo.save(reservation.getSourcePort());

    // Destination port stuff
    reservation.getDestinationPort().getPhysicalResourceGroup().setId(null);
    instituteRepo.save(reservation.getDestinationPort().getPhysicalResourceGroup().getInstitute());
    physicalResourceGroupRepo.save(reservation.getDestinationPort().getPhysicalPort().getPhysicalResourceGroup());

    reservation.getDestinationPort().getPhysicalPort().setId(null);
    physicalPortRepo.save(reservation.getDestinationPort().getPhysicalPort());

    virtualResourceGroupRepo.save(reservation.getDestinationPort().getVirtualResourceGroup());

    reservation.getDestinationPort().setId(null);
    virtualPortRepo.save(reservation.getDestinationPort());

    reservation.setId(null);
    reservationRepo.save(reservation);

    reservationRepo.flush();
  }
}