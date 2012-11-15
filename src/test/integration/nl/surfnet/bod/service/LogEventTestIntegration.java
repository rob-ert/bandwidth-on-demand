package nl.surfnet.bod.service;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.event.LogEventType;
import nl.surfnet.bod.repo.LogEventRepo;
import nl.surfnet.bod.support.LogEventFactory;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/appCtx.xml", "/spring/appCtx-jpa-test.xml",
    "/spring/appCtx-nbi-client.xml", "/spring/appCtx-idd-client.xml" })
@Transactional
public class LogEventTestIntegration {

  private final static Long ONE = 1L;
  private final static DateTime now = DateTime.now();

  @Resource
  private LogEventRepo logEventRepo;

  @Resource
  private LogEventService subject;

  @BeforeTransaction
  public void setUp() {
    LogEvent logEventCreate = new LogEventFactory().setEventType(LogEventType.CREATE).setDomainObjectId(ONE)
        .setCreated(now.minusHours(4)).create();

    LogEvent logEventToScheduled = new LogEventFactory().setOldReservationStatus(ReservationStatus.RESERVED)
        .setNewReservationStatus(ReservationStatus.SCHEDULED).setDomainObjectId(ONE).setCreated(now.minusHours(3))
        .create();

    LogEvent logEventToRunning = new LogEventFactory().setOldReservationStatus(ReservationStatus.SCHEDULED)
        .setNewReservationStatus(ReservationStatus.RUNNING).setDomainObjectId(ONE).setCreated(now.minusHours(2))
        .create();

    LogEvent logEventToSucceeded = new LogEventFactory().setOldReservationStatus(ReservationStatus.RUNNING)
        .setNewReservationStatus(ReservationStatus.SUCCEEDED).setDomainObjectId(ONE).setCreated(now.minusHours(1))
        .create();

    logEventRepo.save(Lists.newArrayList(logEventCreate, logEventToScheduled, logEventToRunning, logEventToSucceeded));
  }

  @Test
  public void shouldFindLatestStateChange() {
    ReservationStatus status = subject.findLatestStateChangeForReservationIdBefore(ONE, now);
    assertThat(status, is(ReservationStatus.SUCCEEDED));
  }

}
