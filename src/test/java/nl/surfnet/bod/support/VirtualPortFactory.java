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

import java.util.concurrent.atomic.AtomicLong;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;

/**
 * Factory for creation of {@link VirtualPort}
 *
 * @author Franky
 *
 */
public class VirtualPortFactory {

  private static final AtomicLong COUNTER = new AtomicLong();

  private Long id = COUNTER.getAndIncrement();
  private Integer version;
  private String name = "A virtual port " + id;
  private VirtualResourceGroup virtualResourceGroup = new VirtualResourceGroupFactory().create();
  private PhysicalPort physicalPort = new PhysicalPortFactory().create();

  public VirtualPort create() {
    VirtualPort virtualPort = new VirtualPort();

    virtualPort.setId(id);
    virtualPort.setVersion(version);
    virtualPort.setName(name);

    virtualPort.setVirtualResourceGroup(virtualResourceGroup);
    virtualPort.setPhysicalPort(physicalPort);

    return virtualPort;
  }

  public VirtualPortFactory setName(String name){
    this.name = name;
    return this;
  }

  public VirtualPortFactory setId(Long id) {
    this.id = id;
    return this;
  }

  public VirtualPortFactory setVirtualResourceGroup(VirtualResourceGroup virtualResourceGroup) {
    this.virtualResourceGroup = virtualResourceGroup;
    return this;
  }

  public VirtualPortFactory setPhysicalPort(PhysicalPort physicalPort) {
    this.physicalPort = physicalPort;
    return this;
  }




}
