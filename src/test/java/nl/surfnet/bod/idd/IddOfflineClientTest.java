package nl.surfnet.bod.idd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

import java.util.Collection;

import nl.surfnet.bod.idd.generated.Klanten;

import org.junit.Test;

public class IddOfflineClientTest {

  private IddOfflineClient subject = new IddOfflineClient();

  @Test
  public void fetchAllKlanten() {
    Collection<Klanten> institutions = subject.getKlanten();

    assertThat(institutions, hasSize(greaterThan(0)));
  }

}
