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

import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.event.LogEventType;
import nl.surfnet.bod.repo.LogEventRepo;
import nl.surfnet.bod.support.LogEventFactory;
import nl.surfnet.bod.support.ReservationFactory;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import static nl.surfnet.bod.domain.ReservationStatus.AUTO_START;
import static nl.surfnet.bod.domain.ReservationStatus.FAILED;
import static nl.surfnet.bod.domain.ReservationStatus.REQUESTED;
import static nl.surfnet.bod.domain.ReservationStatus.RESERVED;
import static nl.surfnet.bod.domain.ReservationStatus.RUNNING;
import static nl.surfnet.bod.domain.ReservationStatus.SCHEDULED;
import static nl.surfnet.bod.domain.ReservationStatus.SUCCEEDED;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/appCtx.xml", "/spring/appCtx-jpa-integration.xml",
    "/spring/appCtx-nbi-client.xml", "/spring/appCtx-idd-client.xml" })
@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
@Transactional
public class LogEventTestIntegration {

  private final static Long ONE = 1L;
  private final static Long TWO = 2L;
  private static final Long THREE = 3L;
  private final static DateTime now = DateTime.now();

  @Resource
  private LogEventRepo logEventRepo;

  @Resource
  private LogEventService subject;

  private List<String> adminGroups;

  @BeforeTransaction
  public void setUp() {
    // Cleanup left overs...
    logEventRepo.deleteAll();

    Reservation reservationOne = new ReservationFactory().setId(ONE).create();
    Reservation reservationTwo = new ReservationFactory().setId(TWO).create();
    Reservation reservationInfinite = new ReservationFactory().setId(THREE).setEndDateTime(null).setStatus(REQUESTED)
        .create();

    adminGroups = Lists.newArrayList(reservationOne.getAdminGroup(), reservationTwo.getAdminGroup(),
        reservationInfinite.getAdminGroup());

    LogEvent logEventCreateOne = createCreateLogEvent(reservationOne, now.minusHours(4));

    LogEvent logEventOneToScheduled = createUpdateLogEvent(reservationOne, RESERVED, SCHEDULED, now.minusHours(3));

    LogEvent logEventOneToRunning = createUpdateLogEvent(reservationOne, SCHEDULED, RUNNING, now.minusHours(2));

    LogEvent logEventToSucceeded = createUpdateLogEvent(reservationOne, RUNNING, SUCCEEDED, now.minusHours(1));

    LogEvent logEventTwoCreate = createCreateLogEvent(reservationTwo, now.minusHours(4));

    LogEvent logEventTwoToAutoStart = createUpdateLogEvent(reservationTwo, RESERVED, AUTO_START, now.minusHours(3));

    LogEvent logEventInfiniteToReserved = createCreateLogEvent(reservationInfinite, now.minusHours(4));

    LogEvent logEventInfiniteToRunning = createUpdateLogEvent(reservationInfinite, AUTO_START, RUNNING, now
        .minusHours(3));

    logEventRepo.save(Lists.newArrayList(logEventCreateOne, logEventOneToScheduled, logEventOneToRunning,
        logEventToSucceeded, logEventTwoCreate, logEventTwoToAutoStart, logEventInfiniteToReserved,
        logEventInfiniteToRunning));

  }

  @Test
  public void shouldFindLogEventsForSetUp() {
    final int expectedAmount = 8;
    assertThat(subject.count(), is(new Long(expectedAmount)));
  }

  @Test
  public void shouldFindLatestStateChangeForReservationOne() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeInAdminGroups(ONE, now, adminGroups);
    assertThat(logEvent.getDomainObjectId(), is(ONE));
    assertTrue(logEvent.getCreated().isBefore(now));
    assertThat(logEvent.getNewReservationStatus(), is(SUCCEEDED));
  }

  @Test
  public void shouldNotFindStateChangeBecauseOfDate() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeInAdminGroups(ONE, now.minusHours(5),
        adminGroups);

    assertThat(logEvent, nullValue());
  }

  @Test
  public void shouldNotFindStateChangeBecauseAdminGroup() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeInAdminGroups(ONE, now.minusHours(5), Lists
        .newArrayList("urn:bla"));

    assertThat(logEvent, nullValue());
  }

  @Test
  public void shouldFindLatestStateChangeToForReservationOne() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeInAdminGroups(ONE, now, adminGroups);

    assertThat(logEvent.getDomainObjectId(), is(ONE));
    assertTrue(logEvent.getCreated().isBefore(now));
    assertThat(logEvent.getNewReservationStatus(), is(SUCCEEDED));
  }

  @Test
  public void shouldFindLatestTransientStateChangeForReservationTwo() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeInAdminGroups(TWO, now, adminGroups);

    assertThat(logEvent.getDomainObjectId(), is(TWO));
    assertTrue(logEvent.getCreated().isBefore(now));
    assertThat(logEvent.getNewReservationStatus(), is(AUTO_START));
  }

  @Test
  public void shouldFindLatestTransientStateTwo() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeInAdminGroups(TWO, now, adminGroups);

    assertThat(logEvent.getDomainObjectId(), is(TWO));
    assertTrue(logEvent.getCreated().isBefore(now));
    assertThat(logEvent.getNewReservationStatus(), is(AUTO_START));
  }

  @Test
  public void shouldCountTransitionFromTo() {
    long count = subject.countStateChangeFromOldToNewForReservationIdBetween(now.minusDays(1), now, RUNNING, SUCCEEDED,
        adminGroups);

    assertThat(count, is(1L));
  }

  @Test
  public void shouldNotCountNonExistingTransitionFromTo() {
    long count = subject.countStateChangeFromOldToNewForReservationIdBetween(now.minusDays(1), now, RUNNING, FAILED,
        adminGroups);

    assertThat(count, is(0L));
  }

  @Test
  public void shouldNotCountTransitionFromToBecauseOfDate() {
    long count = subject.countStateChangeFromOldToNewForReservationIdBetween(now, now, RUNNING, SUCCEEDED, adminGroups);

    assertThat(count, is(0L));
  }

  @Test
  public void shouldFindLatestStateChangeForReservationOneAt4HoursAgo() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeInAdminGroups(ONE, now.minusHours(4),
        adminGroups);
    assertThat(logEvent.getDomainObjectId(), is(ONE));
    assertThat(logEvent.getOldReservationStatus(), is(REQUESTED));
    assertThat(logEvent.getNewReservationStatus(), is(RESERVED));
  }

  @Test
  public void shouldFindLatestStateChangeForReservationOneAt3HoursAgo() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeInAdminGroups(ONE, now.minusHours(3),
        adminGroups);

    assertThat(logEvent.getDomainObjectId(), is(ONE));
    assertThat(logEvent.getOldReservationStatus(), is(RESERVED));
    assertThat(logEvent.getNewReservationStatus(), is(SCHEDULED));
  }

  @Test
  public void shouldFindLatestStateChangeForReservationOneAt2HoursAgo() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeInAdminGroups(ONE, now.minusHours(2),
        adminGroups);

    assertThat(logEvent.getDomainObjectId(), is(ONE));
    assertThat(logEvent.getOldReservationStatus(), is(SCHEDULED));
    assertThat(logEvent.getNewReservationStatus(), is(RUNNING));
  }

  @Test
  public void shouldFindLatestStateChangeForReservationOneAt1HoursAgo() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeInAdminGroups(ONE, now.minusHours(1),
        adminGroups);

    assertThat(logEvent.getDomainObjectId(), is(ONE));
    assertThat(logEvent.getOldReservationStatus(), is(RUNNING));
    assertThat(logEvent.getNewReservationStatus(), is(SUCCEEDED));
  }

  @Test
  public void shouldFindLatestStateChangeForReservationOneAt1HoursAgoCornerCase() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeInAdminGroups(ONE, now.minusHours(1)
        .minusSeconds(1), adminGroups);

    assertThat(logEvent.getDomainObjectId(), is(ONE));
    assertThat(logEvent.getOldReservationStatus(), is(SCHEDULED));
    assertThat(logEvent.getNewReservationStatus(), is(RUNNING));
  }

  @Test
  public void shouldFindLatestStateChangeForInfiniteReservationAt4HoursAgo() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeInAdminGroups(THREE, now.minusHours(4),
        adminGroups);

    assertThat(logEvent.getDomainObjectId(), is(THREE));
    assertThat(logEvent.getOldReservationStatus(), is(REQUESTED));
    assertThat(logEvent.getNewReservationStatus(), is(RESERVED));
  }

  @Test
  public void shouldFindLatestStateChangeForInfiniteReservationAt3HoursAgo() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeInAdminGroups(THREE, now.minusHours(3),
        adminGroups);

    assertThat(logEvent.getDomainObjectId(), is(THREE));
    assertThat(logEvent.getOldReservationStatus(), is(AUTO_START));
    assertThat(logEvent.getNewReservationStatus(), is(RUNNING));
  }

  private LogEvent createUpdateLogEvent(Reservation reservation, ReservationStatus oldStatus,
      ReservationStatus newStatus, DateTime timeStamp) {
    return new LogEventFactory().setEventType(LogEventType.UPDATE).setOldReservationStatus(oldStatus)
        .setNewReservationStatus(newStatus).setDomainObject(reservation).setCreated(timeStamp).setAdminGroup(
            reservation.getAdminGroup()).create();
  }

  private LogEvent createCreateLogEvent(Reservation reservation, DateTime timeStamp) {
    LogEvent logEventCreateOne = new LogEventFactory().setEventType(LogEventType.CREATE).setDomainObject(reservation)
        .setCreated(timeStamp).setAdminGroup(reservation.getAdminGroup()).create();
    return logEventCreateOne;
  }
}
