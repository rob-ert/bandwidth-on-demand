package nl.surfnet.bod.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.support.ConnectionFactory;
import nl.surfnet.bod.support.ReservationFactory;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

public class ReservationArchiveTest {

  @Test
  public void testReservationArchive() throws Exception {
    final ObjectMapper mapper = new ReservationService().getObjectMapper();

    assertThat(mapper.canSerialize(Reservation.class), is(true));

    final Connection connection = new ConnectionFactory().create();
    final Reservation reservation = new ReservationFactory().setConnection(connection).create();

//    ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
//    System.out.println(writer.writeValueAsString(reservation));

    final String reservationAsJson = mapper.writeValueAsString(reservation);

    final Reservation reservationFromJson = mapper.readValue(reservationAsJson, Reservation.class);
    final Connection connectionFromJson = reservationFromJson.getConnection().get();

    assertThat(reservation.getStartDate(), is(reservationFromJson.getStartDate()));
    assertThat(reservation.getEndDate(), is(reservationFromJson.getEndDate()));
    assertThat(reservation.getDestinationPort().getAdminGroup(), is(reservationFromJson.getDestinationPort()
        .getAdminGroup()));
    assertThat(reservation.getDestinationPort().getNsiStpId(), is(reservationFromJson.getDestinationPort()
        .getNsiStpId()));

    assertThat(connection.getConnectionId(), is(connectionFromJson.getConnectionId()));
    assertThat(connection.getCurrentState(), is(connectionFromJson.getCurrentState()));
  }

}
