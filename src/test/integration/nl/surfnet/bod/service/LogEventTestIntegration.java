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

import nl.surfnet.bod.domain.Reservation;
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
import static nl.surfnet.bod.domain.ReservationStatus.TRANSITION_STATES_AS_ARRAY;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/appCtx.xml", "/spring/appCtx-jpa-test.xml",
    "/spring/appCtx-nbi-client.xml", "/spring/appCtx-idd-client.xml" })
@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
@Transactional
public class LogEventTestIntegration {

  private final static Long ONE = 1L;
  private final static Long TWO = 2L;
  private final static DateTime now = DateTime.now();

  @Resource
  private LogEventRepo logEventRepo;

  @Resource
  private LogEventService subject;

  @BeforeTransaction
  public void setUp() {

    Reservation reservationOne = new ReservationFactory().setId(ONE).create();
    Reservation reservationTwo = new ReservationFactory().setId(TWO).create();

    LogEvent logEventCreateOne = new LogEventFactory().setEventType(LogEventType.CREATE)
        .setDomainObject(reservationOne).setCreated(now.minusHours(4)).create();

    LogEvent logEventOneToScheduled = new LogEventFactory().setOldReservationStatus(RESERVED).setNewReservationStatus(
        SCHEDULED).setDomainObject(reservationOne).setCreated(now.minusHours(3)).create();

    LogEvent logEventOneToRunning = new LogEventFactory().setOldReservationStatus(SCHEDULED).setNewReservationStatus(
        RUNNING).setDomainObject(reservationOne).setCreated(now.minusHours(2)).create();

    LogEvent logEventToSucceeded = new LogEventFactory().setOldReservationStatus(RUNNING).setNewReservationStatus(
        SUCCEEDED).setDomainObject(reservationOne).setCreated(now.minusHours(1)).create();

    LogEvent logEventTwoCreate = new LogEventFactory().setEventType(LogEventType.CREATE)
        .setDomainObject(reservationTwo).setCreated(now.minusHours(4)).create();

    LogEvent logEventTwoToAutoStart = new LogEventFactory().setOldReservationStatus(RESERVED).setNewReservationStatus(
        AUTO_START).setDomainObject(reservationTwo).setCreated(now.minusHours(3)).create();

    logEventRepo.save(Lists.newArrayList(logEventCreateOne, logEventOneToScheduled, logEventOneToRunning,
        logEventToSucceeded, logEventTwoCreate, logEventTwoToAutoStart));
  }

  @Test
  public void shouldFindLogEventsForSetUp() {
    final int expectedAmount = 6;
    assertThat(subject.count(), is(new Long(expectedAmount)));

    for (LogEvent logEvent : subject.findAll(0, expectedAmount, null)) {
      System.err.println(logEvent);
    }
  }

  @Test
  public void shouldFindLatestStateChangeForReservationOne() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeWithStateIn(ONE, now);
    assertThat(logEvent.getDomainObjectId(), is(ONE));
    assertTrue(logEvent.getCreated().isBefore(now));
    assertThat(logEvent.getNewReservationStatus(), is(SUCCEEDED));
  }

  @Test
  public void shouldNotFindStateChangeBecauseOfDate() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeWithStateIn(ONE, now.minusHours(5));

    assertThat(logEvent, nullValue());
  }

  @Test
  public void shouldFindLatestStateChangeToForReservationOne() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeWithStateIn(ONE, now, RESERVED);

    assertThat(logEvent.getDomainObjectId(), is(ONE));
    assertTrue(logEvent.getCreated().isBefore(now));
    assertThat(logEvent.getNewReservationStatus(), is(RESERVED));
  }

  @Test
  public void shouldNotFindLatestStateChangeToForReservationOne() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeWithStateIn(ONE, now, FAILED);

    assertThat(logEvent, nullValue());
  }

  @Test
  public void shouldFindLatestTransientStateOne() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeWithStateIn(ONE, now,
        TRANSITION_STATES_AS_ARRAY);
    assertThat(logEvent.getDomainObjectId(), is(ONE));
    assertTrue(logEvent.getCreated().isBefore(now));
    assertThat(logEvent.getNewReservationStatus(), is(RUNNING));
  }

  @Test
  public void shouldFindLatestTransientStateChangeForReservationTwo() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeWithStateIn(TWO, now,
        TRANSITION_STATES_AS_ARRAY);

    assertThat(logEvent.getDomainObjectId(), is(TWO));
    assertTrue(logEvent.getCreated().isBefore(now));
    assertThat(logEvent.getNewReservationStatus(), is(AUTO_START));
  }

  @Test
  public void shouldFindLatestTransientStateTwo() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeWithStateIn(TWO, now,
        TRANSITION_STATES_AS_ARRAY);

    assertThat(logEvent.getDomainObjectId(), is(TWO));
    assertTrue(logEvent.getCreated().isBefore(now));
    assertThat(logEvent.getNewReservationStatus(), is(AUTO_START));
  }

  @Test
  public void shouldFindTransitionFromTo() {
    LogEvent logEvent = subject.findStateChangeFromOldToNewForReservationIdBefore(RUNNING, SUCCEEDED, ONE, now);

    assertThat(logEvent.getDomainObjectId(), is(ONE));
    assertTrue(logEvent.getCreated().isBefore(now));
    assertThat(logEvent.getOldReservationStatus(), is(RUNNING));
    assertThat(logEvent.getNewReservationStatus(), is(SUCCEEDED));
  }

  @Test
  public void shouldNotFindTransitionFromToBecauseOfDate() {
    LogEvent logEvent = subject.findStateChangeFromOldToNewForReservationIdBefore(RUNNING, SUCCEEDED, ONE, now
        .minusDays(1));

    assertThat(logEvent, nullValue());
  }

  @Test
  public void shouldNotFindTransitionFromTo() {
    LogEvent logEvent = subject.findStateChangeFromOldToNewForReservationIdBefore(RUNNING, FAILED, ONE, now);

    assertThat(logEvent, nullValue());
  }

  @Test
  public void shouldFindLatestStateChangeForReservationOneAt4HoursAgo() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeWithStateIn(ONE, now.minusHours(4));
    assertThat(logEvent.getDomainObjectId(), is(ONE));
    assertThat(logEvent.getOldReservationStatus(), is(REQUESTED));
    assertThat(logEvent.getNewReservationStatus(), is(RESERVED));
  }

  @Test
  public void shouldFindLatestStateChangeForReservationOneAt3HoursAgo() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeWithStateIn(ONE, now.minusHours(3));

    assertThat(logEvent.getDomainObjectId(), is(ONE));
    assertThat(logEvent.getOldReservationStatus(), is(RESERVED));
    assertThat(logEvent.getNewReservationStatus(), is(SCHEDULED));
  }

  @Test
  public void shouldFindLatestStateChangeForReservationOneAt2HoursAgo() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeWithStateIn(ONE, now.minusHours(2));

    assertThat(logEvent.getDomainObjectId(), is(ONE));
    assertThat(logEvent.getOldReservationStatus(), is(SCHEDULED));
    assertThat(logEvent.getNewReservationStatus(), is(RUNNING));
  }

  @Test
  public void shouldFindLatestStateChangeForReservationOneAt1HoursAgo() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeWithStateIn(ONE, now.minusHours(1));

    assertThat(logEvent.getDomainObjectId(), is(ONE));
    assertThat(logEvent.getOldReservationStatus(), is(RUNNING));
    assertThat(logEvent.getNewReservationStatus(), is(SUCCEEDED));
  }

  @Test
  public void shouldFindLatestStateChangeForReservationOneAt1HoursAgoCornerCase() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeWithStateIn(ONE, now.minusHours(1)
        .minusSeconds(1));

    assertThat(logEvent.getDomainObjectId(), is(ONE));
    assertThat(logEvent.getOldReservationStatus(), is(SCHEDULED));
    assertThat(logEvent.getNewReservationStatus(), is(RUNNING));
  }

}
