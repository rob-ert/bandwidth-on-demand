package nl.surfnet.bod.idd;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Collection;

import org.junit.Test;

public class IddOfflineClientTest {

  private IddOfflineClient subject = new IddOfflineClient();

  @Test
  public void fetchAllKlanten() {
    Collection<Klanten> institutions = subject.getKlanten();

    assertThat(institutions, hasSize(greaterThan(0)));
  }

}
