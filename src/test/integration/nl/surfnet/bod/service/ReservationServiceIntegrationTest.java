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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.repo.PhysicalPortRepo;
import nl.surfnet.bod.repo.PhysicalResourceGroupRepo;
import nl.surfnet.bod.repo.ReservationRepo;
import nl.surfnet.bod.repo.VirtualPortRepo;
import nl.surfnet.bod.repo.VirtualResourceGroupRepo;
import nl.surfnet.bod.support.ReservationFactory;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/appCtx.xml", "/spring/appCtx-jpa-test.xml",
    "/spring/appCtx-nbi-client.xml", "/spring/appCtx-idd-client.xml" })
@Transactional
@Ignore("Fix persisting reservation tree")
public class ReservationServiceIntegrationTest {

  @Autowired
  private ReservationService reservationService;

  @Autowired
  private ReservationRepo reservationRepo;

  @Autowired
  private VirtualResourceGroupRepo virtualResourceGroupRepo;

  @Autowired
  private VirtualPortRepo virtualPortRepo;

  @Autowired
  private PhysicalPortRepo physicalPortRepo;

  @Autowired
  private PhysicalResourceGroupRepo physicalResourceGroupRepo;

  private Reservation reservationOne;
  private Reservation reservationTwo;
  private Reservation reservationThree;

  private LocalDateTime now = new LocalDateTime();
  private LocalDateTime yesterday = now.minusDays(1);
  private LocalDateTime tomorrow = now.plusDays(1);
  private Specification<Reservation> reservationsForStatusChange;

  @Before
  public void setUp() {
    reservationsForStatusChange = reservationService.specFutureReservationsForStatusChange(now);

    reservationOne = new ReservationFactory().setStartDate(yesterday.toLocalDate())
        .setStartTime(yesterday.toLocalTime()).setStatus(ReservationStatus.PREPARING).create();

    reservationTwo = new ReservationFactory().setStartDate(now.toLocalDate()).setStartTime(now.toLocalTime())
        .setStatus(ReservationStatus.SCHEDULED).create();

    reservationThree = new ReservationFactory().setStartDate(tomorrow.toLocalDate())
        .setStartTime(tomorrow.toLocalTime()).setStatus(ReservationStatus.RUNNING).create();

    persistReservation(reservationOne);
    persistReservation(reservationTwo);
    persistReservation(reservationThree);
  }

  private void persistReservation(Reservation reservation) {

    // Source port stuff
    reservation.getSourcePort().getPhysicalResourceGroup().setId(null);
    physicalResourceGroupRepo.save(reservation.getSourcePort().getPhysicalPort().getPhysicalResourceGroup());

    reservation.getSourcePort().getPhysicalPort().setId(null);
    physicalPortRepo.save(reservation.getSourcePort().getPhysicalPort());

    reservation.getSourcePort().setId(null);
    virtualPortRepo.save(reservation.getSourcePort());

    // Destination port stuff
    reservation.getDestinationPort().getPhysicalResourceGroup().setId(null);
    physicalResourceGroupRepo.save(reservation.getDestinationPort().getPhysicalPort().getPhysicalResourceGroup());

    reservation.getDestinationPort().getPhysicalPort().setId(null);
    physicalPortRepo.save(reservation.getDestinationPort().getPhysicalPort());

    reservation.getDestinationPort().setId(null);
    virtualPortRepo.save(reservation.getDestinationPort());

    // Reservation stuff
    reservation.getVirtualResourceGroup().setId(null);
    virtualResourceGroupRepo.save(reservation.getVirtualResourceGroup());

    reservation.setId(null);
    reservationRepo.save(reservation);

    reservationRepo.flush();
  }

  @Test
  public void shouldNotFindReservations() {
    List<Reservation> reservations = reservationRepo.findAll(reservationsForStatusChange);
    assertThat(reservations.size(), is((2)));
  }

}
