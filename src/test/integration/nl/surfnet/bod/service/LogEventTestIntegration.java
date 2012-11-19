package nl.surfnet.bod.service;

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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

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

    LogEvent logEventOneToScheduled = new LogEventFactory().setOldReservationStatus(ReservationStatus.RESERVED)
        .setNewReservationStatus(ReservationStatus.SCHEDULED).setDomainObject(reservationOne).setCreated(
            now.minusHours(3)).create();

    LogEvent logEventOneToRunning = new LogEventFactory().setOldReservationStatus(ReservationStatus.SCHEDULED)
        .setNewReservationStatus(ReservationStatus.RUNNING).setDomainObject(reservationOne).setCreated(
            now.minusHours(2)).create();

    LogEvent logEventToSucceeded = new LogEventFactory().setOldReservationStatus(ReservationStatus.RUNNING)
        .setNewReservationStatus(ReservationStatus.SUCCEEDED).setDomainObject(reservationOne).setCreated(
            now.minusHours(1)).create();

    LogEvent logEventTwoCreate = new LogEventFactory().setEventType(LogEventType.CREATE)
        .setDomainObject(reservationTwo).setCreated(now.minusHours(4)).create();

    LogEvent logEventTwoToAutoStart = new LogEventFactory().setOldReservationStatus(ReservationStatus.RESERVED)
        .setNewReservationStatus(ReservationStatus.AUTO_START).setDomainObject(reservationTwo).setCreated(
            now.minusHours(3)).create();

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
    assertThat(logEvent.getNewReservationStatus(), is(ReservationStatus.SUCCEEDED));
  }

  @Test
  public void shouldFindLatestStateChangeToForReservationOne() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeWithStateIn(ONE, now,
        ReservationStatus.RESERVED);

    assertThat(logEvent.getDomainObjectId(), is(ONE));
    assertThat(logEvent.getNewReservationStatus(), is(ReservationStatus.RESERVED));
  }

  @Test
  public void shouldNotFindLatestStateChangeToForReservationOne() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeWithStateIn(ONE, now,
        ReservationStatus.FAILED);

    assertThat(logEvent, nullValue());
  }

  @Test
  public void shouldFindLatestTransientStateOne() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeWithStateIn(ONE, now,
        ReservationStatus.TRANSITION_STATES_AS_ARRAY);
    assertThat(logEvent.getDomainObjectId(), is(ONE));
    assertThat(logEvent.getNewReservationStatus(), is(ReservationStatus.RUNNING));
  }

  @Test
  public void shouldFindLatestTransientStateChangeForReservationTwo() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeWithStateIn(TWO, now,
        ReservationStatus.TRANSITION_STATES_AS_ARRAY);

    assertThat(logEvent.getDomainObjectId(), is(TWO));
    assertThat(logEvent.getNewReservationStatus(), is(ReservationStatus.AUTO_START));
  }

  @Test
  public void shouldFindLatestTransientStateTwo() {
    LogEvent logEvent = subject.findLatestStateChangeForReservationIdBeforeWithStateIn(TWO, now,
        ReservationStatus.TRANSITION_STATES_AS_ARRAY);

    assertThat(logEvent.getDomainObjectId(), is(TWO));
    assertThat(logEvent.getNewReservationStatus(), is(ReservationStatus.AUTO_START));
  }

}
