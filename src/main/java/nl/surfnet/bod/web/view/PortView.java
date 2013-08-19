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

import com.google.common.base.Optional;

import nl.surfnet.bod.domain.ReservationEndPoint;
import nl.surfnet.bod.domain.VirtualPort;

public class PortView {
  private final String userLabel;
  private final String managerLabel;
  private final String physicalPortNocLabel;
  private final String physicalPortManagerLabel;
  private final String bodPortId;
  private final String institute;
  // FIXME add VLAN ID

  public PortView(ReservationEndPoint reservationEndPoint) {
    this.bodPortId = reservationEndPoint.getPhysicalPort().getBodPortId();
    this.physicalPortNocLabel = reservationEndPoint.getPhysicalPort().getNocLabel();
    Optional<VirtualPort> virtualPort = reservationEndPoint.getVirtualPort();
    if (virtualPort.isPresent()) {
      this.userLabel = virtualPort.get().getUserLabel();
      this.managerLabel = virtualPort.get().getManagerLabel();
      this.physicalPortManagerLabel = virtualPort.get().getPhysicalPort().getManagerLabel();
      this.institute = virtualPort.get().getPhysicalResourceGroup().getInstitute().getName();
    } else {
      this.userLabel = "-";
      this.managerLabel = "-";
      this.physicalPortManagerLabel = "-";
      this.institute = "-";
    }
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

  public String getInstitute() {
    return institute;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((bodPortId == null) ? 0 : bodPortId.hashCode());
    result = prime * result + ((institute == null) ? 0 : institute.hashCode());
    result = prime * result + ((managerLabel == null) ? 0 : managerLabel.hashCode());
    result = prime * result + ((physicalPortManagerLabel == null) ? 0 : physicalPortManagerLabel.hashCode());
    result = prime * result + ((physicalPortNocLabel == null) ? 0 : physicalPortNocLabel.hashCode());
    result = prime * result + ((userLabel == null) ? 0 : userLabel.hashCode());
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
    PortView other = (PortView) obj;
    if (bodPortId == null) {
      if (other.bodPortId != null) {
        return false;
      }
    }
    else if (!bodPortId.equals(other.bodPortId)) {
      return false;
    }
    if (institute == null) {
      if (other.institute != null) {
        return false;
      }
    }
    else if (!institute.equals(other.institute)) {
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
    if (physicalPortManagerLabel == null) {
      if (other.physicalPortManagerLabel != null) {
        return false;
      }
    }
    else if (!physicalPortManagerLabel.equals(other.physicalPortManagerLabel)) {
      return false;
    }
    if (physicalPortNocLabel == null) {
      if (other.physicalPortNocLabel != null) {
        return false;
      }
    }
    else if (!physicalPortNocLabel.equals(other.physicalPortNocLabel)) {
      return false;
    }
    if (userLabel == null) {
      if (other.userLabel != null) {
        return false;
      }
    }
    else if (!userLabel.equals(other.userLabel)) {
      return false;
    }
    return true;
  }

}