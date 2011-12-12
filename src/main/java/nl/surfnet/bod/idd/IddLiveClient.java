package nl.surfnet.bod.idd;

import java.util.Arrays;
import java.util.Collection;

import nl.surfnet.bod.idd.generated.InvoerKlant;
import nl.surfnet.bod.idd.generated.Klanten;
import nl.surfnet.bod.idd.generated.KsrBindingStub;
import nl.surfnet.bod.idd.generated.KsrLocator;

import org.springframework.beans.factory.annotation.Value;

public class IddLiveClient implements IddClient {

  private static final String IDD_VERSION = "1.09";

  @Value("${idd.user}")
  private String username;

  @Value("${idd.password}")
  private String password;

  @Value("${idd.url}")
  private String endPoint;

  @Override
  public Collection<Klanten> getKlanten() {
    try {
      KsrLocator locator = new KsrLocator();
      locator.setksrPortEndpointAddress(endPoint);

      KsrBindingStub port = (KsrBindingStub) locator.getksrPort();
      port.setUsername(username);
      port.setPassword(password);

      Klanten[] klantnamen = port.getKlantList(new InvoerKlant("list", "", IDD_VERSION)).getKlantnamen();
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

  protected void setEndPoint(String endPoint) {
    this.endPoint = endPoint;
  }

}
