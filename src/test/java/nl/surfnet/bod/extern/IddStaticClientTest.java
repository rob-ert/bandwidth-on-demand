package nl.surfnet.bod.extern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

import java.util.Collection;

import nl.surfnet.bod.idd.Klanten;
import nl.surfnet.bod.idd.client.IddClientMock;

import org.junit.Test;

public class IddStaticClientTest {

  private IddClientMock subject = new IddClientMock();

  @Test
  public void fetchAllKlanten() {
    Collection<Klanten> institutions = subject.getKlanten();

    assertThat(institutions, hasSize(greaterThan(0)));
  }

}
