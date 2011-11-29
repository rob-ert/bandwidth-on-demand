package nl.surfnet.bod.extern;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.PostConstruct;

import nl.surfnet.bod.idd.Klanten;
import nl.surfnet.bod.idd.Klantnamen;

import org.apache.axis.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

public class IddClientMock implements IddClient {

    private final Logger logger = LoggerFactory.getLogger(IddClientMock.class);

    private String staticResponseFile = "/idd_response.xml";
    
    @SuppressWarnings("unused")
    @PostConstruct
    private void init(){
        logger.warn("U S I N G  M O C K  I D D  C L I E N T!");
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
        } catch (Exception e) {
            logger.error("Could not load the institutions", e);
            return new Klanten[] {};
        }
    }

    private Message getStaticMessage() {
        return new Message(IddClientMock.class.getResourceAsStream(staticResponseFile));
    }
    
    public void setUsername(final String username) {
    }

    public void setPassword(final String password) {
    }
}
