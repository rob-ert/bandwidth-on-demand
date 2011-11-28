package nl.surfnet.bod.extern;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import nl.surfnet.bod.idd.Klanten;

import org.junit.Test;

public class IddLiveClientTestIntegration {

    private IddLiveClient subject = new IddLiveClient();

    @Test
    public void callIddService() {
        Collection<Klanten> klanten = subject.getKlanten();

        assertThat(klanten, hasSize(greaterThan(0)));
    }

}
