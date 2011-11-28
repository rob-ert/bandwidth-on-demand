package nl.surfnet.bod.service;

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.Institution;
import nl.surfnet.bod.idd.InvoerKlant;
import nl.surfnet.bod.idd.Klanten;
import nl.surfnet.bod.idd.KsrBindingStub;
import nl.surfnet.bod.idd.KsrLocator;
import nl.surfnet.bod.idd.KsrPortType;

import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Service("institutionIddService")
public class InstitutionIddService implements InstitutionService {

    @Override
    public Collection<Institution> getInstitutions() {

        try {
            KsrPortType port = new KsrLocator().getksrPort();

            ((KsrBindingStub) port).setUsername("extern");
            ((KsrBindingStub) port).setPassword("mattheus");

            Klanten[] klantnamen = port.getKlantList(new InvoerKlant("list", "", "1.09")).getKlantnamen();

            List<Institution> institutions = Lists.newArrayList();
            for (int i = 0; i < klantnamen.length; i++) {
                String name = klantnamen[i].getKlantnaam().trim();
                if (Strings.isNullOrEmpty(name)) {
                    continue;
                }
                institutions.add(new Institution(name));
            }

            return institutions;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
