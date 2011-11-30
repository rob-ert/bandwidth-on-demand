package nl.surfnet.bod.idd;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.PostConstruct;

import org.apache.axis.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IddOfflineClient implements IddClient {

  private final Logger logger = LoggerFactory.getLogger(IddOfflineClient.class);

  private String staticResponseFile = "/idd_response.xml";

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    logger.info("USING OFFLINE IDD CLIENT!");
  }

  @Override
  public Collection<Klanten> getKlanten() {
    Message message = getStaticMessage();

    Klanten[] klantnamen = extractKlantNamen(message);

    return Arrays.asList(klantnamen);
  }

  private Klanten[] extractKlantNamen(Message message) {
    Klantnamen result;
    try {
      result = (Klantnamen) message.getSOAPEnvelope().getFirstBody().getObjectValue(Klantnamen.class);
      return result.getKlantnamen();
    }
    catch (Exception e) {
      logger.error("Could not load the institutions", e);
      return new Klanten[] {};
    }
  }

  private Message getStaticMessage() {
    return new Message(IddOfflineClient.class.getResourceAsStream(staticResponseFile));
  }

  public void setUsername(final String username) {
  }

  public void setPassword(final String password) {
  }
}
