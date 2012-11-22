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

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.repo.ReservationRepo;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = true)
@ContextConfiguration(locations = { "/spring/appCtx.xml", "/spring/appCtx-jpa-integration.xml",
    "/spring/appCtx-nbi-client.xml", "/spring/appCtx-idd-client.xml" })
@Transactional
public class ReportReservationServiceDbTest {
  private final static long AMOUNT_OF_RESERVATIONS = 9;

  // override bod.properties to run test and bod server at the same time
  static {
    System.setProperty("snmp.host", "localhost/1622");
  }

  @PersistenceContext
  private EntityManager entityManager;

  @Resource
  private ReservationServiceDbTestHelper helper;

  @Resource
  private ReservationService reservationService;

  @Resource
  private ReservationRepo reservationRepo;

  private final DateTime periodStart = DateTime.now().plusHours(1);
  private final DateTime periodEnd = periodStart.plusDays(1);

  private Reservation reservationOnStartPeriod;
  private Reservation reservationOnEndPeriod;
  private Reservation reservationBeforeStartAndAfterEndPeriod;
  private Reservation reservationBeforeStartAndOnEndPeriod;
  private Reservation reservationAfterStartAndOnEndPeriod;
  private Reservation reservationAfterStartAndAfterEndPeriod;

  private Reservation reservationBeforePeriod;
  private Reservation reservationInPeriod;
  private Reservation reservationAfterPeriod;

  @BeforeTransaction
  public void setUp() {
    reservationOnStartPeriod = helper.createAndPersist(periodStart, periodEnd.plusDays(1), ReservationStatus.REQUESTED);

    reservationOnEndPeriod = helper.createAndPersist(periodStart, periodEnd.plusDays(1), ReservationStatus.REQUESTED);
    reservationBeforeStartAndAfterEndPeriod = helper.createAndPersist(periodStart, periodEnd.plusDays(1),
        ReservationStatus.REQUESTED);
    reservationBeforeStartAndOnEndPeriod = helper.createAndPersist(periodStart, periodEnd.plusDays(1),
        ReservationStatus.REQUESTED);
    reservationAfterStartAndOnEndPeriod = helper.createAndPersist(periodStart, periodEnd.plusDays(1),
        ReservationStatus.REQUESTED);
    reservationAfterStartAndAfterEndPeriod = helper.createAndPersist(periodStart, periodEnd.plusDays(1),
        ReservationStatus.REQUESTED);

    reservationBeforePeriod = helper.createAndPersist(periodStart, periodEnd.plusDays(1), ReservationStatus.REQUESTED);
    reservationInPeriod = helper.createAndPersist(periodStart, periodEnd.plusDays(1), ReservationStatus.REQUESTED);
    reservationAfterPeriod = helper.createAndPersist(periodStart, periodEnd.plusDays(1), ReservationStatus.REQUESTED);
  }

  @AfterTransaction
  public void teardown() {
    helper.cleanUp();
  }

  @Test
  public void checkSetup() {
    long amountOfReservations = reservationService.count();

    assertThat(amountOfReservations, is(AMOUNT_OF_RESERVATIONS));
  }
}