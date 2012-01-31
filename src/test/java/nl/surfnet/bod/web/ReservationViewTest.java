package nl.surfnet.bod.web;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.VirtualPortFactory;

import org.junit.Test;

public class ReservationViewTest {

  @Test
  public void reservationViewShouldShowUserLabel() {
    VirtualPort sourcePort = new VirtualPortFactory().setManagerLabel("Label of boss").setUserLabel("My source label").create();
    VirtualPort destPort = new VirtualPortFactory().setManagerLabel("Label of boss").setUserLabel("My dest label").create();
    Reservation reservation = new ReservationFactory().setSourcePort(sourcePort).setDestinationPort(destPort).create();

    ReservationView view = new ReservationView(reservation);

    assertThat(view.getSourcePort(), is("My source label"));
    assertThat(view.getDestinationPort(), is("My dest label"));
  }

}
