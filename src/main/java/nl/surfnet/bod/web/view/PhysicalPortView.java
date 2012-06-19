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

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalPort;

public class PhysicalPortView {

  private final Long id;
  private final String managerLabel;
  private final String nocLabel;
  private final String bodPortId;
  private final String instituteName;
  private final String nmsPortId;
  private ElementActionView deleteActionView;
  private Long numberOfVirtualPorts;
  private final boolean vlanRequired;
  private final boolean alignedWithNMS;
  private boolean deleteRender;

  public PhysicalPortView(final PhysicalPort physicalPort, final ElementActionView deleteActionView,
      final long virtualPortSize) {

    this(physicalPort, deleteActionView);
    this.numberOfVirtualPorts = virtualPortSize;
  }

  public PhysicalPortView(final PhysicalPort physicalPort, final ElementActionView deleteActionView) {
    this(physicalPort);

    if (deleteActionView == null) {
      this.deleteRender = false;
    }
    else {
      this.deleteActionView = deleteActionView;
    }
  }

  public PhysicalPortView(final PhysicalPort physicalPort) {
    this.id = physicalPort.getId();
    this.managerLabel = physicalPort.getManagerLabel();
    this.nocLabel = physicalPort.getNocLabel();
    this.bodPortId = physicalPort.getBodPortId();
    this.instituteName = physicalPort.getPhysicalResourceGroup() == null ? null : physicalPort
        .getPhysicalResourceGroup().getName();
    this.nmsPortId = physicalPort.getNmsPortId();
    this.vlanRequired = physicalPort.isVlanRequired();
    this.alignedWithNMS = physicalPort.isAlignedWithNMS();

    this.numberOfVirtualPorts = 0L;
    this.deleteActionView = new ElementActionView(false, "");
    this.deleteRender = true;
  }

  public String getDeleteReasonKey() {
    return deleteActionView.getReasonKey();
  }

  public boolean isDeleteAllowed() {
    return deleteActionView.isAllowed();
  }

  public long getId() {
    return id;
  }

  public String getNocLabel() {
    return nocLabel;
  }

  public String getBodPortId() {
    return bodPortId;
  }

  public String getInstituteName() {
    return instituteName;
  }

  public String getNmsPortId() {
    return nmsPortId;
  }

  public ElementActionView getDeleteActionView() {
    return deleteActionView;
  }

  public String getManagerLabel() {
    return managerLabel;
  }

  public long getNumberOfVirtualPorts() {
    return numberOfVirtualPorts;
  }

  public boolean isDeleteRender() {
    return deleteRender;
  }

  public boolean isVlanRequired() {
    return vlanRequired;
  }

  public boolean isAlignedWithNMS() {
    return alignedWithNMS;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (alignedWithNMS ? 1231 : 1237);
    result = prime * result + ((deleteActionView == null) ? 0 : deleteActionView.hashCode());
    result = prime * result + (deleteRender ? 1231 : 1237);
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((managerLabel == null) ? 0 : managerLabel.hashCode());
    result = prime * result + ((nmsPortId == null) ? 0 : nmsPortId.hashCode());
    result = prime * result + ((nocLabel == null) ? 0 : nocLabel.hashCode());
    result = prime * result + ((numberOfVirtualPorts == null) ? 0 : numberOfVirtualPorts.hashCode());
    result = prime * result + ((instituteName == null) ? 0 : instituteName.hashCode());
    result = prime * result + ((bodPortId == null) ? 0 : bodPortId.hashCode());
    result = prime * result + (vlanRequired ? 1231 : 1237);
    return result;
  }

  @Override
  public String toString() {
    return "PhysicalPortView [id=" + id + ", managerLabel=" + managerLabel + ", nocLabel=" + nocLabel + ", bodPortId="
        + bodPortId + ", instituteName=" + instituteName + ", nmsPortId=" + nmsPortId
        + ", deleteActionView=" + deleteActionView + ", numberOfVirtualPorts=" + numberOfVirtualPorts
        + ", vlanRequired=" + vlanRequired + ", alignedWithNMS=" + alignedWithNMS + ", deleteRender=" + deleteRender
        + "]";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PhysicalPortView other = (PhysicalPortView) obj;
    if (alignedWithNMS != other.alignedWithNMS)
      return false;
    if (deleteActionView == null) {
      if (other.deleteActionView != null)
        return false;
    }
    else if (!deleteActionView.equals(other.deleteActionView))
      return false;
    if (deleteRender != other.deleteRender)
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    if (managerLabel == null) {
      if (other.managerLabel != null)
        return false;
    }
    else if (!managerLabel.equals(other.managerLabel))
      return false;
    if (nmsPortId == null) {
      if (other.nmsPortId != null)
        return false;
    }
    else if (!nmsPortId.equals(other.nmsPortId))
      return false;
    if (nocLabel == null) {
      if (other.nocLabel != null)
        return false;
    }
    else if (!nocLabel.equals(other.nocLabel))
      return false;
    if (numberOfVirtualPorts == null) {
      if (other.numberOfVirtualPorts != null)
        return false;
    }
    else if (!numberOfVirtualPorts.equals(other.numberOfVirtualPorts))
      return false;
    if (instituteName == null) {
      if (other.instituteName != null)
        return false;
    }
    else if (!instituteName.equals(other.instituteName))
      return false;
    if (bodPortId == null) {
      if (other.bodPortId != null)
        return false;
    }
    else if (!bodPortId.equals(other.bodPortId))
      return false;
    if (vlanRequired != other.vlanRequired)
      return false;
    return true;
  }

}
