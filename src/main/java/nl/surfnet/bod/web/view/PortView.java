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

public class PortView {
  private final String userLabel;
  private final String managerLabel;
  private final String physicalPortNocLabel;
  private final String physicalPortManagerLabel;
  private final String bodPortId;

  public PortView(VirtualPort port) {
    this.userLabel = port.getUserLabel();
    this.managerLabel = port.getManagerLabel();
    this.physicalPortManagerLabel = port.getPhysicalPort().getManagerLabel();
    this.physicalPortNocLabel = port.getPhysicalPort().getNocLabel();
    this.bodPortId = port.getPhysicalPort().getBodPortId();
  }

  public String getUserLabel() {
    return userLabel;
  }

  public String getManagerLabel() {
    return managerLabel;
  }

  public String getPhysicalPortNocLabel() {
    return physicalPortNocLabel;
  }

  public String getPhysicalPortManagerLabel() {
    return physicalPortManagerLabel;
  }

  public String getBodPortId() {
    return bodPortId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((bodPortId == null) ? 0 : bodPortId.hashCode());
    result = prime * result + ((managerLabel == null) ? 0 : managerLabel.hashCode());
    result = prime * result + ((physicalPortManagerLabel == null) ? 0 : physicalPortManagerLabel.hashCode());
    result = prime * result + ((physicalPortNocLabel == null) ? 0 : physicalPortNocLabel.hashCode());
    result = prime * result + ((userLabel == null) ? 0 : userLabel.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PortView other = (PortView) obj;
    if (bodPortId == null) {
      if (other.bodPortId != null)
        return false;
    }
    else if (!bodPortId.equals(other.bodPortId))
      return false;
    if (managerLabel == null) {
      if (other.managerLabel != null)
        return false;
    }
    else if (!managerLabel.equals(other.managerLabel))
      return false;
    if (physicalPortManagerLabel == null) {
      if (other.physicalPortManagerLabel != null)
        return false;
    }
    else if (!physicalPortManagerLabel.equals(other.physicalPortManagerLabel))
      return false;
    if (physicalPortNocLabel == null) {
      if (other.physicalPortNocLabel != null)
        return false;
    }
    else if (!physicalPortNocLabel.equals(other.physicalPortNocLabel))
      return false;
    if (userLabel == null) {
      if (other.userLabel != null)
        return false;
    }
    else if (!userLabel.equals(other.userLabel))
      return false;
    return true;
  }

}