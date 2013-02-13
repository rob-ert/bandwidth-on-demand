package nl.surfnet.bod.search;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.List;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.support.ConnectionFactory;
import nl.surfnet.bod.support.ReservationFactory;

import org.apache.lucene.queryParser.ParseException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;


public class ConnectionIndexAndSearchTest extends AbstractIndexAndSearch<Connection> {

  public ConnectionIndexAndSearchTest() {
    super(Connection.class);
  }

  @Before
  public void setupSearchData() {
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.FAILED).withNodId().create();
    Connection connection = new ConnectionFactory()
      .setReservation(reservation)
      .setCurrentState(ConnectionStateType.TERMINATED)
      .withNoId().create();

    persist(connection);
  }

  @Test
  public void findConnectionByItsCurrentState() throws ParseException {

    List<Connection> connections = searchFor("TERMINATED");

    assertThat(connections, hasSize(1));
  }

  @Test
  @Ignore("Can not search from connection to reservation, results in circulair reference")
  public void findConnectionByItsReservationStatus() throws ParseException {

    List<Connection> connections = searchFor("FAILED");

    assertThat(connections, hasSize(1));
  }

  private void persist(Connection connection) {
    persist(
      connection.getReservation().getSourcePort().getVirtualResourceGroup(),
      connection.getReservation().getSourcePort(),
      connection.getReservation().getDestinationPort(),
      connection.getReservation(),
      connection);
  }

}