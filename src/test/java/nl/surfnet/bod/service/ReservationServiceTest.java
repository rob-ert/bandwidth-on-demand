package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.support.ReservationFactory;

import org.junit.Before;
import org.junit.Test;

public class ReservationServiceTest {

  private ReservationService subject;
  
  @Before
  public void onSetUp() {
    subject = new ReservationService();
  }

  @Test
  public void testMakeReservation() {
    Reservation reservation = new ReservationFactory().create();

    ReservationStatus reservationStatus = subject.makeReservation(reservation);

    assertThat(reservation.getReservationStatus(), is(reservationStatus.PENDING));
  }

}
