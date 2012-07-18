package nl.surfnet.bod.mtosi;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MtosiLiveClientTest {

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
  public void convertPortName() {
    final String mtosiPortName = "/rack=1/shelf=1/slot=1/port=48";
    final String expectedPortName = "1-1-1-48";
    final String convertedPortName = new MtosiLiveClient(null, null).convertPortName(mtosiPortName);
    assertThat(convertedPortName, equalTo(expectedPortName));
  }

  @Test
  public void convertSubPortName() {
    final String mtosiPortName = "/rack=1/shelf=1/slot=3/sub_slot=1";
    final String expectedPortName = "1-1-3-1";
    final String convertedPortName = new MtosiLiveClient(null, null).convertPortName(mtosiPortName);
    assertThat(convertedPortName, equalTo(expectedPortName));
  }

}
