package nl.surfnet.bod.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.support.ReservationFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class ReservationPollerTest {
  private final Logger log = LoggerFactory.getLogger(getClass());

  @InjectMocks
  private ReservationPoller subject;

  @Mock
  private ReservationService reservationService;

  private final Reservation reservationOne = new ReservationFactory().setStatus(ReservationStatus.PREPARING)
      .setReservationId("123").create();

  private final int maxTries = 2;

  @Before
  public void setUp() {
    subject.init(1, maxTries);
  }

  @Test
  public void shouldBeEnabled() {
    assertFalse(subject.isMonitoringDisabled());
  }

  @Test
  public void shouldBeDisabled() {
    subject.init(1, -1);
    assertTrue(subject.isMonitoringDisabled());
  }

  @Test
  public void shouldUpdateState() throws InterruptedException {
    // Prevent NPE, since the update method must return the saved entity
    when(reservationService.update(any(Reservation.class))).thenReturn(reservationOne);

    when(reservationService.getStatus(any(Reservation.class))).thenReturn(ReservationStatus.PREPARING,
        ReservationStatus.SCHEDULED);

    subject.monitorStatus(reservationOne);

    waitWhilePollerIsDone(reservationOne);

    verify(reservationService).update(reservationOne);
  }

  @Test
  public void shouldNotUpdateStateNoStateChange() throws InterruptedException {
    // Prevent NPE, since the update method must return the saved entity
    when(reservationService.update(any(Reservation.class))).thenReturn(reservationOne);

    // Same state as initial state, no change
    when(reservationService.getStatus(any(Reservation.class))).thenReturn(ReservationStatus.PREPARING);

    subject.monitorStatus(reservationOne);

    waitWhilePollerIsDone(reservationOne);

    verify(reservationService, times(0)).update(any(Reservation.class));
  }

  @Test
  public void shouldStopWhenMaxTriesIsReached() throws InterruptedException {
    // Prevent NPE, since the update method must return the saved entity
    when(reservationService.update(any(Reservation.class))).thenReturn(reservationOne);

    // Same state as initial state, no change
    when(reservationService.getStatus(any(Reservation.class))).thenReturn(ReservationStatus.PREPARING);

    subject.monitorStatus(reservationOne);

    waitWhilePollerIsDone(reservationOne);

    verify(reservationService, times(maxTries)).getStatus(any(Reservation.class));
  }

  @Test
  public void shouldStopWhenEndStateIsReached() throws InterruptedException {
    // Prevent NPE, since the update method must return the saved entity
    when(reservationService.update(any(Reservation.class))).thenReturn(reservationOne);

    when(reservationService.getStatus(any(Reservation.class))).thenReturn(ReservationStatus.SUCCEEDED);
    when(reservationService.isEndState(any(ReservationStatus.class))).thenReturn(true);

    subject.monitorStatus(reservationOne);

    waitWhilePollerIsDone(reservationOne);

    verify(reservationService).update(reservationOne);
    verify(reservationService).getStatus(any(Reservation.class));
  }

  private void waitWhilePollerIsDone(Reservation reservation) {
    while (reservation.isCurrentlyProcessing()) {
      log.debug("Waiting while reservation {} is processed.", reservation);
    }
  }

}
