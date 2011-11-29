package nl.surfnet.bod.service;

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.Institution;
import nl.surfnet.bod.extern.IddClient;
import nl.surfnet.bod.idd.Klanten;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.googlecode.ehcache.annotations.Cacheable;

@Service
public class InstitutionIddService implements InstitutionService {

  private IddClient iddClient;

  @Override
  @Cacheable(cacheName = "institutionsCache")
  public Collection<Institution> getInstitutions() {
    Collection<Klanten> klanten = iddClient.getKlanten();

    return toInstitutions(klanten);
  }

  private Collection<Institution> toInstitutions(Collection<Klanten> klantnamen) {
    List<Institution> institutions = Lists.newArrayList();
    for (Klanten klant : klantnamen) {
      String klantnaam = klant.getKlantnaam().trim();
      if (Strings.isNullOrEmpty(klantnaam)) {
        continue;
      }
      institutions.add(new Institution(klantnaam));
    }

    return institutions;
  }

  @Autowired
  public void setIddClient(IddClient iddClient) {
    this.iddClient = iddClient;
  }
}
