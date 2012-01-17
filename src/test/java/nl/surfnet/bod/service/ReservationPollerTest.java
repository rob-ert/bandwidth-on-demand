package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.support.ReservationFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReservationPollerTest {

  @InjectMocks
  private ReservationPoller reservationPoller;

  @Mock
  private ReservationService reservationService;

  @Test
  public void shouldUpdateState() throws InterruptedException {
    when(reservationService.getStatus(any(Reservation.class))).thenReturn(ReservationStatus.PREPARING,
        ReservationStatus.SCHEDULED);

    Reservation reservationOne = new ReservationFactory().setStatus(ReservationStatus.PREPARING)
        .setReservationId("123").create();

    reservationPoller.getStatus(reservationOne);

    while (reservationPoller.isBusy());
      
    assertThat(reservationPoller.isBusy(), is(false));
    verify(reservationService).update(reservationOne);
  }

  @Test
  public void shouldNotUpdateStateNoStateChange() throws InterruptedException {
    // Same state as initial state, no change
    when(reservationService.getStatus(any(Reservation.class))).thenReturn(ReservationStatus.PREPARING);

    Reservation reservationOne = new ReservationFactory().setStatus(ReservationStatus.PREPARING)
        .setReservationId("123").create();

    reservationPoller.getStatus(reservationOne);

    while (reservationPoller.isBusy());
    
    assertThat(reservationPoller.isBusy(), is(false));
    verify(reservationService, times(0)).update(any(Reservation.class));

  }

}
