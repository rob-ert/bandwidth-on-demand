package nl.surfnet.bod.web.push;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.service.ReservationStatusChangeEvent;
import nl.surfnet.bod.support.ReservationFactory;

import org.junit.Test;

public class EventsTest {

  @Test
  public void aReservationStatusChangedEventShouldHaveAJsonMessage() {
    Reservation reservation = new ReservationFactory().setId(54L).setStatus(ReservationStatus.SCHEDULED).create();

    ReservationStatusChangeEvent reservationStatusChangeEvent = new ReservationStatusChangeEvent(ReservationStatus.PREPARING, reservation);    
    
    Event event = Events.createEvent(reservationStatusChangeEvent);

    assertThat(event.getMessage(), containsString("\"id\":54"));
    assertThat(event.getMessage(), containsString("from PREPARING to SCHEDULED"));
    assertThat(event.getMessage(), containsString("\"status\":\"SCHEDULED\""));
    assertThat(event.getGroupId(), is(reservation.getVirtualResourceGroup().getSurfConextGroupName()));
  }

}
