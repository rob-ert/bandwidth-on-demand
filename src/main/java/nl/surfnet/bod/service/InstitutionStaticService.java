package nl.surfnet.bod.service;

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.Institution;
import nl.surfnet.bod.idd.Klanten;
import nl.surfnet.bod.idd.Klantnamen;

import org.apache.axis.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Service("institutionStaticService")
public class InstitutionStaticService implements InstitutionService {

    private final Logger logger = LoggerFactory.getLogger(InstitutionStaticService.class);

    @Override
    public Collection<Institution> getInstitutions() {
        Message message = getStaticMessage();

        Klanten[] klantnamen = extractKlantNamen(message);

        return toInstitutions(klantnamen);
    }

    private Collection<Institution> toInstitutions(Klanten[] klantnamen) {
        List<Institution> institutions = Lists.newArrayList();
        for (int i = 0; i < klantnamen.length; i++) {
            String klantnaam = klantnamen[i].getKlantnaam();
            if (Strings.isNullOrEmpty(klantnaam)) {
                continue;
            }
            institutions.add(new Institution(klantnaam));
        }

        return institutions;
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
        return new Message(InstitutionStaticService.class.getResourceAsStream("/idd_response.xml"));
    }

}
