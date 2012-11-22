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
import org.joda.time.DateTimeUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static nl.surfnet.bod.domain.ReservationStatus.AUTO_START;
import static nl.surfnet.bod.domain.ReservationStatus.REQUESTED;

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

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @PersistenceContext
  private EntityManager entityManager;

  @Resource
  private ReservationServiceDbTestHelper helper;

  @Resource
  private ReservationRepo reservationRepo;

  @Resource
  private ReservationService subject;

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
    logger.warn("Start of period [{}], end [{}]", periodStart, periodEnd);
    helper.cleanUp();

    // Five (5) reservations in reporting period
    reservationOnStartPeriod = createReservation(periodStart, periodEnd.plusDays(1), ReservationStatus.REQUESTED);
    reservationOnEndPeriod = createReservation(periodStart.plusHours(1), periodEnd, ReservationStatus.REQUESTED);
    reservationAfterStartAndOnEndPeriod = createReservation(periodStart.plusHours(1), periodEnd,
        ReservationStatus.REQUESTED);
    reservationAfterStartAndAfterEndPeriod = createReservation(periodStart.plusHours(1), periodEnd.plusDays(1),
        ReservationStatus.REQUESTED);
    reservationInPeriod = createReservation(periodStart.plusHours(1), periodEnd.minusHours(1),
        ReservationStatus.REQUESTED);

    // Two (2) reservations related to reporting period
    reservationBeforeStartAndAfterEndPeriod = createReservation(periodStart.minusHours(1), periodEnd.plusDays(1),
        ReservationStatus.REQUESTED);
    reservationBeforeStartAndOnEndPeriod = createReservation(periodStart.minusHours(1), periodEnd,
        ReservationStatus.REQUESTED);

    // Two (2) reservations not related to reporting period
    reservationBeforePeriod = createReservation(periodStart.minusDays(1), periodStart.minusHours(1),
        ReservationStatus.REQUESTED);
    reservationAfterPeriod = createReservation(periodEnd.plusHours(1), periodEnd.plusDays(1),
        ReservationStatus.REQUESTED);
  }

  @AfterTransaction
  public void teardown() {
    helper.cleanUp();
  }

  @Test
  public void verifyToSaveALotOfSetupTime() {
    checkSetup();

    shouldCountExistingStateInPeriod();
    shouldCountExistingStateBeforePeriodOnCorner();
    shouldCountExistingStateBeforePeriod();
    shouldCountExistingStateAfterPeriod();
    shouldCountNonExistingStateInPeriod();
    shouldCountExsitingTransitionInPeriod();
    shouldCountNonExsitingTransitionInPeriod();
  }

  private void checkSetup() {
    long amountOfReservations = subject.count();

    assertThat(amountOfReservations, is(AMOUNT_OF_RESERVATIONS));
  }

  private void shouldCountExistingStateInPeriod() {
    long count = subject.countReservationsForNocWhichHadStateBetween(periodStart, periodEnd,
        ReservationStatus.REQUESTED);

    assertThat(count, is(5L));
  }

  private void shouldCountExistingStateBeforePeriodOnCorner() {
    long count = subject.countReservationsForNocWhichHadStateBetween(periodStart.minusDays(2), periodStart
        .minusHours(1), ReservationStatus.REQUESTED);

    assertThat(count, is(3L));
  }

  private void shouldCountExistingStateBeforePeriod() {
    long count = subject.countReservationsForNocWhichHadStateBetween(periodStart.minusDays(2), periodStart
        .minusHours(2), ReservationStatus.REQUESTED);

    assertThat(count, is(1L));
  }

  private void shouldCountExistingStateAfterPeriod() {
    long count = subject.countReservationsForNocWhichHadStateBetween(periodEnd.plusDays(2), periodEnd.plusDays(3),
        ReservationStatus.REQUESTED);

    assertThat(count, is(0L));
  }

  private void shouldCountNonExistingStateInPeriod() {
    long count = subject
        .countReservationsForNocWhichHadStateBetween(periodStart, periodEnd, ReservationStatus.RESERVED);

    assertThat(count, is(0L));
  }

  private void shouldCountExsitingTransitionInPeriod() {
    long count = subject.countReservationsForNocWhichHadStateTransitionBetween(periodStart, periodEnd, REQUESTED,
        AUTO_START);

    assertThat(count, is(5L));
  }

  private void shouldCountNonExsitingTransitionInPeriod() {
    long count = subject.countReservationsForNocWhichHadStateTransitionBetween(periodStart, periodEnd, REQUESTED,
        ReservationStatus.NOT_ACCEPTED);

    assertThat(count, is(0L));
  }

  private Reservation createReservation(DateTime start, DateTime end, ReservationStatus status) {
    // Make sure all events are created with the time related to the reservation
    DateTimeUtils.setCurrentMillisFixed(start.getMillis());
    try {
      Reservation reservation = helper.createReservation(start, end, status);
      return helper.createThroughService(reservation);
    }
    finally {
      DateTimeUtils.currentTimeMillis();
    }
  }
}