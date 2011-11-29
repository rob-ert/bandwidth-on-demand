package nl.surfnet.bod.idd;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;

public class IddClientImplTestIntegration {

  private IddClientImpl subject = new IddClientImpl();

  @Test
  public void callIddService() {
    Collection<Klanten> klanten = subject.getKlanten();

    assertThat(klanten, hasSize(greaterThan(0)));
  }

}
