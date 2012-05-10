package nl.surfnet.bod.web.tag;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class FunctionsTest {

  @Test
  public void shouldReplaceNewLines() {
    String output = Functions.translateNewLineBr("\n hallo\n\n");

    assertThat(output, is("<br/> hallo<br/><br/>"));
  }

}
