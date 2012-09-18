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

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.idd.generated.Klanten;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.web.view.ElementActionView;
import nl.surfnet.bod.web.view.PhysicalPortView;
import nl.surfnet.bod.web.view.UserGroupView;
import nl.surfnet.bod.web.view.VirtualPortView;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public final class Functions {

  public static final Function<PhysicalPort, String> TO_NMS_PORT_ID_FUNC = new Function<PhysicalPort, String>() {
    @Override
    public String apply(PhysicalPort physicalPort) {
      return physicalPort.getNmsPortId();
    }
  };

  public static final Predicate<PhysicalPort> MISSING_PORTS_PRED = new Predicate<PhysicalPort>() {
    @Override
    public boolean apply(PhysicalPort physicalPort) {
      return !physicalPort.isAlignedWithNMS();
    }
  };

  public static final Predicate<PhysicalPort> NON_MISSING_PORTS_PRED = new Predicate<PhysicalPort>() {
    @Override
    public boolean apply(PhysicalPort physicalPort) {
      return !MISSING_PORTS_PRED.apply(physicalPort);
    }
  };

  public static final Function<UserGroup, UserGroupView> FROM_USER_GROUP_TO_USER_GROUP_VIEW = new Function<UserGroup, UserGroupView>() {
    @Override
    public UserGroupView apply(UserGroup group) {
      return new UserGroupView(group);
    }
  };

  public static final Function<VirtualResourceGroup, UserGroupView> FROM_VRG_TO_USER_GROUP_VIEW = new Function<VirtualResourceGroup, UserGroupView>() {
    @Override
    public UserGroupView apply(VirtualResourceGroup group) {
      return new UserGroupView(group);
    }
  };

  public static final Function<VirtualPort, VirtualPortView> FROM_VIRTUALPORT_TO_VIRTUALPORT_VIEW = new Function<VirtualPort, VirtualPortView>() {
    @Override
    public VirtualPortView apply(VirtualPort port) {
      return new VirtualPortView(port);
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

    List<PhysicalPortView> transformers = Lists.newArrayList();
    for (PhysicalPort port : ports) {
      transformers.add(transformAllocatedPhysicalPort(port, virtualPortService));
    }

    return transformers;
  }

  public static PhysicalPortView transformUnallocatedPhysicalPort(PhysicalPort unallocatedPort) {
    return new PhysicalPortView(unallocatedPort, null);
  }

  public static List<PhysicalPortView> transformUnallocatedPhysicalPorts(Collection<PhysicalPort> unallocatedPorts) {
    List<PhysicalPortView> transformers = Lists.newArrayList();
    for (PhysicalPort port : unallocatedPorts) {
      transformers.add(transformUnallocatedPhysicalPort(port));
    }

    return transformers;
  }

  public static Optional<Institute> transformKlant(Klanten klant, boolean alignedWithIDD) {
    if (Strings.isNullOrEmpty(klant.getKlantnaam()) && Strings.isNullOrEmpty(klant.getKlantafkorting())) {
      return Optional.absent();
    }

    return Optional.of(new Institute(Long.valueOf(klant.getKlant_id()), trimIfNotNull(klant.getKlantnaam()),
        trimIfNotNull(klant.getKlantafkorting()), alignedWithIDD));
  }

  private static String trimIfNotNull(String value) {
    return value != null ? value.trim() : value;
  }

  public static Collection<Institute> transformKlanten(Collection<Klanten> klanten, final boolean alignedWithIDD) {
    Collection<Optional<Institute>> institutes = Collections2.transform(klanten,
        new Function<Klanten, Optional<Institute>>() {
          @Override
          public Optional<Institute> apply(Klanten klant) {
            return transformKlant(klant, alignedWithIDD);
          }
        });

    return Lists.newArrayList(Optional.presentInstances(institutes));
  }

}
