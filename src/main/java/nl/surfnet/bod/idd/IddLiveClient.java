package nl.surfnet.bod.idd;

import java.util.Arrays;
import java.util.Collection;

import nl.surfnet.bod.idd.generated.InvoerKlant;
import nl.surfnet.bod.idd.generated.Klanten;
import nl.surfnet.bod.idd.generated.KsrBindingStub;
import nl.surfnet.bod.idd.generated.KsrLocator;
import nl.surfnet.bod.idd.generated.KsrPortType;

import org.springframework.beans.factory.annotation.Value;

public class IddLiveClient implements IddClient {

  @Value("${idd.user}")
  private String username;

  @Value("${idd.password}")
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

  protected void setUsername(String username) {
    this.username = username;
  }

  protected void setPassword(String password) {
    this.password = password;
  }

}
