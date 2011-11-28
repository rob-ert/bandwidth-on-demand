package nl.surfnet.bod.extern;

import java.util.Arrays;
import java.util.Collection;

import nl.surfnet.bod.idd.InvoerKlant;
import nl.surfnet.bod.idd.Klanten;
import nl.surfnet.bod.idd.KsrBindingStub;
import nl.surfnet.bod.idd.KsrLocator;
import nl.surfnet.bod.idd.KsrPortType;

import org.springframework.stereotype.Component;

@Component("iddLiveClient")
public class IddLiveClient implements IddClient {

    @Override
    public Collection<Klanten> getKlanten() {
        try {
            KsrPortType port = new KsrLocator().getksrPort();

            ((KsrBindingStub) port).setUsername("extern");
            ((KsrBindingStub) port).setPassword("mattheus");

            Klanten[] klantnamen = port.getKlantList(new InvoerKlant("list", "", "1.09")).getKlantnamen();

            return Arrays.asList(klantnamen);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
