/**
 * Copyright (c) 2012, 2013 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.service;

import static nl.surfnet.bod.domain.ReservationStatus.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.AppConfiguration;
import nl.surfnet.bod.config.IntegrationDbConfiguration;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AppConfiguration.class, IntegrationDbConfiguration.class })
@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
@Transactional
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@ActiveProfiles("opendrac-offline")
public class LogEventTestIntegration {

  private final static Long ONE = 1L;
  private final static Long TWO = 2L;
  private static final Long THREE = 3L;
  private final static DateTime now = DateTime.now();

  @Resource private LogEventRepo logEventRepo;
  @Resource private LogEventService subject;

  private List<String> adminGroups;

  @BeforeTransaction
  public void setUp() {
    // Cleanup left overs...
    logEventRepo.deleteAll();

    Reservation reservationOne = new ReservationFactory().setId(ONE).create();
    Reservation reservationTwo = new ReservationFactory().setId(TWO).create();
    Reservation reservationInfinite = new ReservationFactory().setId(THREE).setEndDateTime(null).setStatus(REQUESTED).create();

    adminGroups = new ArrayList<>();
    adminGroups.addAll(reservationOne.getAdminGroups());
    adminGroups.addAll(reservationTwo.getAdminGroups());
    adminGroups.addAll(reservationInfinite.getAdminGroups());

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
        .setNewReservationStatus(newStatus).setDomainObject(reservation).setCreated(timeStamp).setAdminGroups(
            reservation.getAdminGroups()).create();
  }

  private LogEvent createCreateLogEvent(Reservation reservation, DateTime timeStamp) {
    LogEvent logEventCreateOne = new LogEventFactory().setEventType(LogEventType.CREATE).setDomainObject(reservation)
        .setCreated(timeStamp).setAdminGroups(reservation.getAdminGroups()).create();
    return logEventCreateOne;
  }
}
