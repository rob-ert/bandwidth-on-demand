package nl.surfnet.bod.service;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.support.ReservationFactory;

import org.joda.time.LocalDateTime;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class ReservationPollerTest {

  @InjectMocks
  private ReservationPoller subject;

  @Mock
  private ReservationEventPublisher reservationEventPublisherMock;
  
  @Mock
  private ReservationService reservationServiceMock;

  @Test
  public void pollerShouldFindOneChangingReservation() throws InterruptedException {
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.SUBMITTED).create();

    when(reservationServiceMock.findReservationsToPoll(any(LocalDateTime.class))).thenReturn(
        Lists.newArrayList(reservation));
    when(reservationServiceMock.getStatus(reservation)).thenReturn(ReservationStatus.SCHEDULED);

    ReservationCountDownListener listener = new ReservationCountDownListener(1);
    reservationEventPublisherMock.addListener(listener);
    
    subject.pollReservations();

    assertTrue(listener.getLatch().await(200, TimeUnit.MILLISECONDS));
    assertThat(listener.getEvents(), hasSize(1));
    assertThat(listener.getEvents().get(0).getOldStatus(), is(ReservationStatus.SUBMITTED));
    assertThat(listener.getEvents().get(0).getReservation().getStatus(), is(ReservationStatus.SCHEDULED));
  }

  @Test
  public void pollerShouldPollMaxTimes() throws InterruptedException {
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.SUBMITTED).create();
    int maxTries = 3;

    when(reservationServiceMock.findReservationsToPoll(any(LocalDateTime.class))).thenReturn(
        Lists.newArrayList(reservation));
    when(reservationServiceMock.getStatus(reservation)).thenReturn(ReservationStatus.SUBMITTED);

    subject.pollReservations();

    verify(reservationServiceMock, times(maxTries)).getStatus(reservation);
  }

  @Test
  public void findReservationShouldBeForWholeMinutes() throws InterruptedException {
    ArgumentCaptor<LocalDateTime> argument = ArgumentCaptor.forClass(LocalDateTime.class);

    subject.pollReservations();

    verify(reservationServiceMock).findReservationsToPoll(argument.capture());

    assertThat(argument.getValue().getSecondOfMinute(), is(0));
    assertThat(argument.getValue().getMillisOfSecond(), is(0));
  }

  private static final class ReservationCountDownListener implements ReservationListener {
    private final CountDownLatch latch;
    private List<ReservationStatusChangeEvent> events = Lists.newArrayList();

    public ReservationCountDownListener(int countDown) {
      latch = new CountDownLatch(countDown);
    }

    @Override
    public void onStatusChange(ReservationStatusChangeEvent event) {
      events.add(event);
      latch.countDown();
    }

    public CountDownLatch getLatch() {
      return latch;
    }

    public List<ReservationStatusChangeEvent> getEvents() {
      return events;
    }
  }

}
