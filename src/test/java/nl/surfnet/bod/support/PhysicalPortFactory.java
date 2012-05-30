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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.PhysicalResourceGroup;

public class PhysicalPortFactory {

  private static final AtomicLong COUNTER = new AtomicLong();

  private Long id = COUNTER.incrementAndGet();
  private String nocLabel = "nameDefault " + id;
  private String managerLabel = "";
  private PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroupFactory().create();
  private Integer version = 0;
  private String networkElementPk = UUID.randomUUID().toString();
  private String portId = "Asd001A_OME3T_ETH-1-1-4";
  private boolean vlanRequired = false;

  public PhysicalPort create() {
    PhysicalPort port = new PhysicalPort(vlanRequired);
    port.setId(id);
    port.setVersion(version);
    port.setPortId(portId);

    port.setNocLabel(nocLabel);
    port.setManagerLabel(managerLabel);
    port.setNetworkElementPk(networkElementPk);
    port.setPhysicalResourceGroup(physicalResourceGroup);
    
    return port;
  }

  public PhysicalPortFactory setId(Long id) {
    this.id = id;
    return this;
  }

  public PhysicalPortFactory setVersion(Integer version) {
    this.version = version;
    return this;
  }

  public PhysicalPortFactory setManagerLabel(String managerLabel) {
    this.managerLabel = managerLabel;
    return this;
  }

  public PhysicalPortFactory setNocLabel(String nocLabel) {
    this.nocLabel = nocLabel;
    return this;
  }

  public PhysicalPortFactory setPhysicalResourceGroup(PhysicalResourceGroup physicalResourceGroup) {
    this.physicalResourceGroup = physicalResourceGroup;
    return this;
  }

  public PhysicalPortFactory setNetworkElementPk(String networkElementPk) {
    this.networkElementPk = networkElementPk;
    return this;
  }

  public PhysicalPortFactory setVlanRequired(boolean vlanRequired) {
    this.vlanRequired = vlanRequired;
    return this;
  }

  public void setPortId(String portId) {
    this.portId = portId;
  }
}
