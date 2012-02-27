package nl.surfnet.bod.web.view;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;

import nl.surfnet.bod.support.ReservationFactory;

import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.LocalDateTime;
import org.junit.Test;

public class ReservationViewJsonRenderTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  public void jodaTimesShouldBePrettyPrinted() throws IOException {
    LocalDateTime startDateTime = new LocalDateTime(2009, 3, 23, 12, 0);

    ReservationView reservationView = new ReservationView(new ReservationFactory().setStartDateTime(startDateTime).create());
    String json = mapper.writer().writeValueAsString(reservationView);

    assertThat(json, containsString("\"startDateTime\":\"2009-03-23 12:00\""));
  }

}
