/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.service;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.idd.IddClient;
import nl.surfnet.bod.idd.generated.Klanten;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Service
public class InstituteIddService implements InstituteService {

  @Autowired
  private IddClient iddClient;

  @Override
  public Collection<Institute> getInstitutes() {
    Collection<Klanten> klanten = iddClient.getKlanten();

    return toInstitutes(klanten.toArray(new Klanten[klanten.size()]));
  }

  private Collection<Institute> toInstitutes(Klanten... klanten) {
    List<Institute> institutes = Lists.newArrayList();
    for (Klanten klant : klanten) {
      if (klant != null) {
        trimAttributes(klant);
        if (!(Strings.isNullOrEmpty(klant.getKlantnaam()) && (Strings.isNullOrEmpty(klant.getKlantafkorting())))) {
          institutes.add(new Institute(Long.valueOf(klant.getKlant_id()), klant.getKlantnaam(), klant
              .getKlantafkorting()));
        }
      }
    }

    return institutes;
  }

  @Override
  public Institute findInstitute(Long id) {
    Institute institute = null;

    Iterator<Institute> it = toInstitutes(iddClient.getKlantById(id)).iterator();
    if (it.hasNext()) {
      institute = it.next();
    }

    return institute;
  }

  private void trimAttributes(Klanten klant) {
    if (!Strings.isNullOrEmpty(klant.getKlantnaam())) {
      klant.setKlantnaam(klant.getKlantnaam().trim());
    }

    if (!Strings.isNullOrEmpty(klant.getKlantafkorting())) {
      klant.setKlantafkorting(klant.getKlantafkorting().trim());
    }

  }

  @Override
  public void fillInstituteForPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
    Preconditions.checkNotNull(physicalResourceGroup);

    if (physicalResourceGroup.getInstituteId() != null) {
      Institute institute = findInstitute(physicalResourceGroup.getInstituteId());
      physicalResourceGroup.setInstitute(institute);
    }
  }

  @Override
  public void fillInstituteForPhysicalResourceGroups(Collection<PhysicalResourceGroup> prgs) {
    for (PhysicalResourceGroup prg : prgs) {
      fillInstituteForPhysicalResourceGroup(prg);
    }
  }

  @Override
  public void fillInstituteForPhysicalPort(PhysicalPort port) {
    fillInstituteForPhysicalResourceGroup(port.getPhysicalResourceGroup());
  }

  @Override
  public void fillInstituteForPhysicalPorts(Collection<PhysicalPort> ports) {

    for (PhysicalPort port : ports) {
      fillInstituteForPhysicalPort(port);
    }
  }

}
