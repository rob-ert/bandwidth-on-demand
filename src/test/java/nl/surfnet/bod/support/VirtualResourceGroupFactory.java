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
package nl.surfnet.bod.support;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;

import com.google.common.collect.Lists;

public class VirtualResourceGroupFactory {

  private static final AtomicLong COUNTER = new AtomicLong();

  private Long id = COUNTER.getAndIncrement();
  private Integer version;
  private String surfconextGroupId = "urn:bandwidth-on-demand" + id;
  private String name = "VRG " + id;
  private String description = "Some virtual resource group";

  private Collection<VirtualPort> virtualPorts = Lists.newArrayList();
  private Collection<Reservation> reservations = Lists.newArrayList();

  public VirtualResourceGroup create() {
    VirtualResourceGroup vrg = new VirtualResourceGroup();

    vrg.setId(id);
    vrg.setVersion(version);
    vrg.setName(name);
    vrg.setDescription(description);
    vrg.setSurfconextGroupId(surfconextGroupId);
    vrg.setVirtualPorts(virtualPorts);
    vrg.setReservations(reservations);

    return vrg;
  }

  public VirtualResourceGroupFactory setId(Long id) {
    this.id = id;
    return this;
  }

  public VirtualResourceGroupFactory setDescription(String description) {
    this.description = description;
    return this;
  }

  public VirtualResourceGroupFactory setVersion(Integer version) {
    this.version = version;
    return this;
  }

  public VirtualResourceGroupFactory setName(String name) {
    this.name = name;
    return this;
  }

  public VirtualResourceGroupFactory setSurfconextGroupId(String surfconextGroupId) {
    this.surfconextGroupId = surfconextGroupId;
    return this;
  }

  public VirtualResourceGroupFactory addVirtualPorts(VirtualPort... ports) {
    this.virtualPorts.addAll(Arrays.asList(ports));
    return this;
  }

  public VirtualResourceGroupFactory addReservations(Reservation... reservations) {
    this.reservations.addAll(Arrays.asList(reservations));
    return this;
  }

}
