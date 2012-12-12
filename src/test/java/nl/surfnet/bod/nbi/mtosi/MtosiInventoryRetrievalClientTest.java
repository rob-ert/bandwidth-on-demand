package nl.surfnet.bod.nbi.mtosi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;


public class MtosiInventoryRetrievalClientTest {

  private MtosiInventoryRetrievalClient subject = new MtosiInventoryRetrievalClient("", "");

  @Test
  public void convertPortName() {
    final String mtosiPortName = "/rack=1/shelf=1/slot=1/port=48";
    final String expectedPortName = "1-1-1-48";
    final String convertedPortName = subject.convertPortName(mtosiPortName);
    assertThat(convertedPortName, is(expectedPortName));
  }

  @Test
  public void convertSubPortName() {
    final String mtosiPortName = "/rack=1/shelf=1/slot=3/sub_slot=1";
    final String expectedPortName = "1-1-3-1";
    final String convertedPortName = subject.convertPortName(mtosiPortName);
    assertThat(convertedPortName, is(expectedPortName));
  }
}
