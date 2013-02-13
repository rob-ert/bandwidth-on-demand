package nl.surfnet.bod.search;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.List;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.support.ConnectionFactory;
import nl.surfnet.bod.support.ReservationFactory;

import org.apache.lucene.queryParser.ParseException;
import org.junit.Test;

public class ReservationIndexAndSearchTest extends AbstractIndexAndSearch<Reservation>{

  public ReservationIndexAndSearchTest() {
    super(Reservation.class);
  }

  @Test
  public void searchAndFindReservationOnNsiConnectionId() throws ParseException {
    Connection connection = new ConnectionFactory().setConnectionId("123-abc-456-def").create();
    Reservation reservation = new ReservationFactory().setConnection(connection).create();

    persist(reservation);

    List<Reservation> reservations = searchFor("123-abc-456-def");

    assertThat(reservations, hasSize(1));
  }

  private void persist(Reservation reservation) {
    persist(
      reservation.getSourcePort().getVirtualResourceGroup(),
      reservation.getSourcePort(),
      reservation.getDestinationPort(),
      reservation
    );
  }

}