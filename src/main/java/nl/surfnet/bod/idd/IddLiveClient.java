package nl.surfnet.bod.idd;

import java.util.Arrays;
import java.util.Collection;

import nl.surfnet.bod.idd.generated.InvoerKlant;
import nl.surfnet.bod.idd.generated.Klanten;
import nl.surfnet.bod.idd.generated.KsrBindingStub;
import nl.surfnet.bod.idd.generated.KsrLocator;
import nl.surfnet.bod.idd.generated.KsrPortType;

public class IddLiveClient implements IddClient {

  private String username;

  private String password;

  @Override
  public Collection<Klanten> getKlanten() {
    try {
      KsrPortType port = new KsrLocator().getksrPort();

      ((KsrBindingStub) port).setUsername(username);
      ((KsrBindingStub) port).setPassword(password);

      Klanten[] klantnamen = port.getKlantList(new InvoerKlant("list", "", "1.09")).getKlantnamen();

      return Arrays.asList(klantnamen);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}
