/**
 * Copyright (c) 2012, 2013 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.web.view;

import nl.surfnet.bod.domain.PhysicalPort;

public class PhysicalPortView {

  private final Long id;
  private final String managerLabel;
  private final String nocLabel;
  private final String bodPortId;
  private final String instituteName;
  private final String nmsPortId;
  private final ElementActionView deleteActionView;
  private final Long numberOfVirtualPorts;
  private final boolean vlanRequired;
  private final boolean alignedWithNMS;
  private boolean deleteRender;

  private final String nmsNeId;
  private final String nmsPortSpeed;
  private final String nmsSapName;

  private int reservationsAmount;

  public PhysicalPortView(PhysicalPort physicalPort, ElementActionView deleteActionView, long virtualPortSize) {
    this.id = physicalPort.getId();
    this.managerLabel = physicalPort.getManagerLabel();
    this.nocLabel = physicalPort.getNocLabel();
    this.bodPortId = physicalPort.getBodPortId();
    this.instituteName = physicalPort.getPhysicalResourceGroup() == null ? null : physicalPort
        .getPhysicalResourceGroup().getName();
    this.nmsPortId = physicalPort.getNmsPortId();
    this.vlanRequired = physicalPort.isVlanRequired();
    this.alignedWithNMS = physicalPort.isAlignedWithNMS();

    this.numberOfVirtualPorts = virtualPortSize;
    this.deleteActionView = deleteActionView;
    this.deleteRender = deleteActionView == null ? false : true;

    this.nmsNeId = physicalPort.getNmsNeId();
    this.nmsPortSpeed = physicalPort.getNmsPortSpeed();
    this.nmsSapName = physicalPort.getNmsSapName();
  }

  public PhysicalPortView(PhysicalPort physicalPort, ElementActionView deleteActionView) {
    this(physicalPort, deleteActionView, 0);
  }

  public PhysicalPortView(PhysicalPort physicalPort) {
    this(physicalPort, new ElementActionView(false, ""), 0L);
  }

  public String getDeleteReasonKey() {
    return deleteActionView == null ? "" : deleteActionView.getReasonKey();
  }

  public boolean isDeleteAllowed() {
    return deleteActionView == null ? false : deleteActionView.isAllowed();
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

  public final String getNmsNeId() {
    return nmsNeId;
  }

  public final String getNmsPortSpeed() {
    return nmsPortSpeed;
  }

  public final String getNmsSapName() {
    return nmsSapName;
  }

  public final int getReservationsAmount() {
    return reservationsAmount;
  }

  public final void setReservationsAmount(int reservationsAmount) {
    this.reservationsAmount = reservationsAmount;
  }

  public final void setDeleteRender(boolean deleteRender) {
    this.deleteRender = deleteRender;
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
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PhysicalPortView other = (PhysicalPortView) obj;
    if (alignedWithNMS != other.alignedWithNMS) {
      return false;
    }
    if (deleteActionView == null) {
      if (other.deleteActionView != null) {
        return false;
      }
    }
    else if (!deleteActionView.equals(other.deleteActionView)) {
      return false;
    }
    if (deleteRender != other.deleteRender) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    }
    else if (!id.equals(other.id)) {
      return false;
    }
    if (managerLabel == null) {
      if (other.managerLabel != null) {
        return false;
      }
    }
    else if (!managerLabel.equals(other.managerLabel)) {
      return false;
    }
    if (nmsPortId == null) {
      if (other.nmsPortId != null) {
        return false;
      }
    }
    else if (!nmsPortId.equals(other.nmsPortId)) {
      return false;
    }
    if (nocLabel == null) {
      if (other.nocLabel != null) {
        return false;
      }
    }
    else if (!nocLabel.equals(other.nocLabel)) {
      return false;
    }
    if (numberOfVirtualPorts == null) {
      if (other.numberOfVirtualPorts != null) {
        return false;
      }
    }
    else if (!numberOfVirtualPorts.equals(other.numberOfVirtualPorts)) {
      return false;
    }
    if (instituteName == null) {
      if (other.instituteName != null) {
        return false;
      }
    }
    else if (!instituteName.equals(other.instituteName)) {
      return false;
    }
    if (bodPortId == null) {
      if (other.bodPortId != null) {
        return false;
      }
    }
    else if (!bodPortId.equals(other.bodPortId)) {
      return false;
    }
    if (vlanRequired != other.vlanRequired) {
      return false;
    }
    return true;
  }

}
