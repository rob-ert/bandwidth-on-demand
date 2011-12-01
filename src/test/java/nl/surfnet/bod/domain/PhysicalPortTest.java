package nl.surfnet.bod.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import nl.surfnet.bod.support.PhysicalPortFactory;

import org.junit.Test;

public class PhysicalPortTest {

  @Test
  public void toStringShouldContainName() {
    PhysicalPort port = new PhysicalPortFactory().setName("pooooort").create();

    assertThat(port.toString(), containsString("pooooort"));
  }
}
