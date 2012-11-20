package nl.surfnet.bod.domain;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.StringWriter;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.support.ConnectionFactory;
import nl.surfnet.bod.support.ReservationFactory;

public class ReservationArchiveTest {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testReservationArchive() throws Exception {
    final ObjectMapper mapper = new ReservationService().getObjectMapper();

    assertThat(mapper.canSerialize(Reservation.class), is(true));

    final Connection connection = new ConnectionFactory().create();
    final Reservation reservation = new ReservationFactory().setConnection(connection).create();
    final StringWriter writer = new StringWriter();

    mapper.writeValue(writer, reservation);

    final String reservationAsJson = writer.toString();
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
