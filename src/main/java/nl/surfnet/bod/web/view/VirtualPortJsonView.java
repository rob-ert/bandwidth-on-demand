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
package nl.surfnet.bod.web.view;

import nl.surfnet.bod.domain.VirtualPort;

public final class VirtualPortJsonView {
  private final String managerLabel;
  private final String userLabel;
  private final Integer maxBandwidth;
  private final Integer vlanId;
  private final String virtualResourceGroupName;
  private Long id;

  public VirtualPortJsonView(VirtualPort port) {
    this.id = port.getId();
    this.managerLabel = port.getManagerLabel();
    this.userLabel = port.getUserLabel();
    this.maxBandwidth = port.getMaxBandwidth();
    this.vlanId = port.getVlanId();
    this.virtualResourceGroupName = port.getVirtualResourceGroup().getName();
  }

  public Integer getMaxBandwidth() {
    return maxBandwidth;
  }

  public Integer getVlanId() {
    return vlanId;
  }

  public String getVirtualResourceGroupName() {
    return virtualResourceGroupName;
  }

  public String getManagerLabel() {
    return managerLabel;
  }

  public String getUserLabel() {
    return userLabel;
  }

  public Long getId() {
    return id;
  }
}