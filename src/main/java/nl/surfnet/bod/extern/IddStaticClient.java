package nl.surfnet.bod.extern;

import java.util.Arrays;
import java.util.Collection;

import nl.surfnet.bod.idd.Klanten;
import nl.surfnet.bod.idd.Klantnamen;

import org.apache.axis.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("iddStaticClient")
public class IddStaticClient implements IddClient {

    private final Logger logger = LoggerFactory.getLogger(IddStaticClient.class);

    private String staticResponseFile = "/idd_response.xml";

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
        return new Message(IddStaticClient.class.getResourceAsStream(staticResponseFile));
    }

    protected void setStaticResponseFile(String responseFile) {
        this.staticResponseFile = responseFile;
    }

}
