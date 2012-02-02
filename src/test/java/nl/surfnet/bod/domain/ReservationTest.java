package nl.surfnet.bod.domain;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import nl.surfnet.bod.support.ReservationFactory;

import org.junit.Test;

public class ReservationTest {

  @Test
  public void toStringShouldContainPrimaryKey() {
    Reservation reservation = new ReservationFactory().setId(24L).create();

    String toString = reservation.toString();

    assertThat(toString, containsString("id=24"));
  }

}
