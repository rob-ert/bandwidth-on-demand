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

import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.nsi.NsiHelper;

public class VirtualPortView {
  private final Long id;
  private final String managerLabel;
  private final Long maxBandwidth;
  private final Integer vlanId;
  private final String virtualResourceGroup;
  private final String physicalResourceGroup;
  private final String physicalPort;
  private final String nmsPortId;
  private final String userLabel;
  private final String nsiProviderIdV1;
  private final String nsiStpIdV1;
  private final String nsiProviderIdV2;
  private final String nsiStpIdV2;
  private final long numberOfActiveReservations;
  private final boolean deleteAllowed;


  public VirtualPortView(VirtualPort port, NsiHelper nsiHelper, long numberOfActiveReservations, boolean deleteAllowed) {
    this.id = port.getId();
    this.managerLabel = port.getManagerLabel();
    this.userLabel = port.getUserLabel();
    this.maxBandwidth = port.getMaxBandwidth();
    this.vlanId = port.getVlanId();
    this.virtualResourceGroup = port.getVirtualResourceGroup().getName();
    this.physicalResourceGroup = port.getPhysicalResourceGroup().getName();
    this.physicalPort = port.getPhysicalPort().getManagerLabel();
    this.nmsPortId = port.getPhysicalPort().getNmsPortId();

    this.nsiProviderIdV1 = nsiHelper.getProviderNsaV1();
    this.nsiStpIdV1 = nsiHelper.getStpIdV1(port);

    this.nsiProviderIdV2 = nsiHelper.getProviderNsaV2();
    this.nsiStpIdV2 = nsiHelper.getStpIdV2(port);

    this.numberOfActiveReservations = numberOfActiveReservations;
    this.deleteAllowed = deleteAllowed;
  }

  public long getNumberOfActiveReservations() {
    return numberOfActiveReservations;
  }

  public boolean isdeleteAllowed() {
    return deleteAllowed;
  }

  public String getManagerLabel() {
    return managerLabel;
  }

  public Long getMaxBandwidth() {
    return maxBandwidth;
  }

  public Integer getVlanId() {
    return vlanId;
  }

  public String getVirtualResourceGroup() {
    return virtualResourceGroup;
  }

  public String getPhysicalResourceGroup() {
    return physicalResourceGroup;
  }

  public String getPhysicalPort() {
    return physicalPort;
  }

  public String getNmsPortId() {
    return nmsPortId;
  }

  public Long getId() {
    return id;
  }

  public String getUserLabel() {
    return userLabel;
  }

  public String getNsiStpIdV1() {
    return nsiStpIdV1;
  }

  public String getNsiStpIdV2() {
    return nsiStpIdV2;
  }

  public String getNsiProviderIdV1() {
    return nsiProviderIdV1;
  }

  public String getNsiProviderIdV2() {
    return nsiProviderIdV2;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("VirtualPortView [");
    if (id != null) {
      builder.append("id=");
      builder.append(id);
      builder.append(", ");
    }
    if (managerLabel != null) {
      builder.append("managerLabel=");
      builder.append(managerLabel);
      builder.append(", ");
    }
    if (maxBandwidth != null) {
      builder.append("maxBandwidth=");
      builder.append(maxBandwidth);
      builder.append(", ");
    }
    if (vlanId != null) {
      builder.append("vlanId=");
      builder.append(vlanId);
      builder.append(", ");
    }
    if (virtualResourceGroup != null) {
      builder.append("virtualResourceGroup=");
      builder.append(virtualResourceGroup);
      builder.append(", ");
    }
    if (physicalResourceGroup != null) {
      builder.append("physicalResourceGroup=");
      builder.append(physicalResourceGroup);
      builder.append(", ");
    }
    if (physicalPort != null) {
      builder.append("physicalPort=");
      builder.append(physicalPort);
      builder.append(", ");
    }
    if (nmsPortId != null) {
      builder.append("nmsPortId=");
      builder.append(nmsPortId);
      builder.append(", ");
    }
    if (userLabel != null) {
      builder.append("userLabel=");
      builder.append(userLabel);
      builder.append(", ");
    }
    if (nsiStpIdV1 != null) {
      builder.append("nsiStpIdV1=");
      builder.append(nsiStpIdV1);
    }
    builder.append("]");
    return builder.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((managerLabel == null) ? 0 : managerLabel.hashCode());
    result = prime * result + ((maxBandwidth == null) ? 0 : maxBandwidth.hashCode());
    result = prime * result + ((nmsPortId == null) ? 0 : nmsPortId.hashCode());
    result = prime * result + ((nsiStpIdV1 == null) ? 0 : nsiStpIdV1.hashCode());
    result = prime * result + ((physicalPort == null) ? 0 : physicalPort.hashCode());
    result = prime * result + ((physicalResourceGroup == null) ? 0 : physicalResourceGroup.hashCode());
    result = prime * result + ((userLabel == null) ? 0 : userLabel.hashCode());
    result = prime * result + ((virtualResourceGroup == null) ? 0 : virtualResourceGroup.hashCode());
    result = prime * result + ((vlanId == null) ? 0 : vlanId.hashCode());
    return result;
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
    VirtualPortView other = (VirtualPortView) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (managerLabel == null) {
      if (other.managerLabel != null) {
        return false;
      }
    } else if (!managerLabel.equals(other.managerLabel)) {
      return false;
    }
    if (maxBandwidth == null) {
      if (other.maxBandwidth != null) {
        return false;
      }
    } else if (!maxBandwidth.equals(other.maxBandwidth)) {
      return false;
    }
    if (nmsPortId == null) {
      if (other.nmsPortId != null) {
        return false;
      }
    } else if (!nmsPortId.equals(other.nmsPortId)) {
      return false;
    }
    if (nsiStpIdV1 == null) {
      if (other.nsiStpIdV1 != null) {
        return false;
      }
    } else if (!nsiStpIdV1.equals(other.nsiStpIdV1)) {
      return false;
    }
    if (physicalPort == null) {
      if (other.physicalPort != null) {
        return false;
      }
    } else if (!physicalPort.equals(other.physicalPort)) {
      return false;
    }
    if (physicalResourceGroup == null) {
      if (other.physicalResourceGroup != null) {
        return false;
      }
    } else if (!physicalResourceGroup.equals(other.physicalResourceGroup)) {
      return false;
    }
    if (userLabel == null) {
      if (other.userLabel != null) {
        return false;
      }
    } else if (!userLabel.equals(other.userLabel)) {
      return false;
    }
    if (virtualResourceGroup == null) {
      if (other.virtualResourceGroup != null) {
        return false;
      }
    } else if (!virtualResourceGroup.equals(other.virtualResourceGroup)) {
      return false;
    }
    if (vlanId == null) {
      if (other.vlanId != null) {
        return false;
      }
    } else if (!vlanId.equals(other.vlanId)) {
      return false;
    }

    return true;
  }

}