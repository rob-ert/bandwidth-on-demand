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
package nl.surfnet.bod.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.idd.generated.Klanten;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.web.view.ElementActionView;
import nl.surfnet.bod.web.view.PhysicalPortView;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;

public final class Functions {

  public static final Function<PhysicalPort, String> TO_NMS_PORT_ID_FUNC = //
  new Function<PhysicalPort, String>() {

    @Override
    public String apply(PhysicalPort physicalPort) {
      return physicalPort.getNmsPortId();
    }
  };

  public static final Predicate<PhysicalPort> MISSING_PORTS_PRED = //
  new Predicate<PhysicalPort>() {
    @Override
    public boolean apply(PhysicalPort physicalPort) {
      return !physicalPort.isAlignedWithNMS();
    }
  };

  public static final Predicate<PhysicalPort> NON_MISSING_PORTS_PRED = //
  new Predicate<PhysicalPort>() {
    @Override
    public boolean apply(PhysicalPort physicalPort) {
      return !MISSING_PORTS_PRED.apply(physicalPort);
    }
  };

  private Functions() {
  }

  /**
   * Calculates the amount of related {@link VirtualPort}s and transforms it to
   * a {@link PhysicalPortView}
   * 
   * @param port
   *          {@link PhysicalPort} to enrich
   * @param virtualPortService
   *          {@link VirtualPortService} to retrieve the amount of related
   *          {@link VirtualPort}s
   * @return PhysicalPortView Transformed {@link PhysicalPort}
   */
  public static PhysicalPortView transformAllocatedPhysicalPort(PhysicalPort port,
      final VirtualPortService virtualPortService) {

    long vpCount = virtualPortService.countForPhysicalPort(port);
    ElementActionView allocateActionView;
    if (vpCount == 0) {
      allocateActionView = new ElementActionView(true, "label_unallocate");
    }
    else {
      allocateActionView = new ElementActionView(false, "label_virtual_ports_related");
    }

    return new PhysicalPortView(port, allocateActionView, vpCount);
  }

  /**
   * Transforms a Collection
   * 
   * @see #transformAllocatedPhysicalPort(PhysicalPort, VirtualPortService)
   * 
   */
  public static List<PhysicalPortView> transformAllocatedPhysicalPorts(List<PhysicalPort> ports,
      final VirtualPortService virtualPortService) {

    List<PhysicalPortView> transformers = new ArrayList<PhysicalPortView>();
    for (PhysicalPort port : ports) {
      transformers.add(transformAllocatedPhysicalPort(port, virtualPortService));
    }

    return transformers;
  }

  public static PhysicalPortView transformUnallocatedPhysicalPort(PhysicalPort unallocatedPort) {
    return new PhysicalPortView(unallocatedPort, null);
  }

  public static List<PhysicalPortView> transformUnallocatedPhysicalPorts(List<PhysicalPort> unallocatedPorts) {
    List<PhysicalPortView> transformers = new ArrayList<PhysicalPortView>();
    for (PhysicalPort port : unallocatedPorts) {
      transformers.add(transformUnallocatedPhysicalPort(port));
    }

    return transformers;
  }

  public static Institute transformKlant(Klanten klant, boolean alignedWithIDD) {
    Institute institute = null;

    if (!(Strings.isNullOrEmpty(klant.getKlantnaam()) && (Strings.isNullOrEmpty(klant.getKlantafkorting())))) {
      institute = new Institute(Long.valueOf(klant.getKlant_id()), klant.getKlantnaam().trim(), klant
          .getKlantafkorting().trim(), alignedWithIDD);
    }

    return institute;
  }

  public static List<Institute> transformKlanten(Collection<Klanten> klanten, boolean alignedWithIDD) {
    List<Institute> transformers = new ArrayList<Institute>();

    Institute institute = null;
    for (Klanten klant : klanten) {
      institute = transformKlant(klant, alignedWithIDD);
      if (institute != null) {
        transformers.add(institute);
      }
    }
    return transformers;
  }
}
