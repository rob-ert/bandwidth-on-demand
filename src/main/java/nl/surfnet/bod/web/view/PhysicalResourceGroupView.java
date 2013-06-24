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

import nl.surfnet.bod.domain.Institute;
import nl.surfnet.bod.domain.PhysicalResourceGroup;

public class PhysicalResourceGroupView {

  private Long id;
  private Institute institute;
  private String adminGroup;
  private String managerEmail;
  private Boolean active = false;

  private int physicalPortsAmount;
  private int virtualPortsAmount;
  private int reservationsAmount;

  public PhysicalResourceGroupView(final PhysicalResourceGroup physicalResourceGroup) {
    this.id = physicalResourceGroup.getId();
    this.institute = physicalResourceGroup.getInstitute();
    this.adminGroup = physicalResourceGroup.getAdminGroup();
    this.active = physicalResourceGroup.isActive();
    this.managerEmail = physicalResourceGroup.getManagerEmail();
  }

  public String getName() {
    return institute != null ? institute.getName() : null;
  }

  public final int getPhysicalPortsAmount() {
    return physicalPortsAmount;
  }

  public final void setPhysicalPortsAmount(int physicalPortsAmount) {
    this.physicalPortsAmount = physicalPortsAmount;
  }

  public final int getVirtualPortsAmount() {
    return virtualPortsAmount;
  }

  public final void setVirtualPortsAmount(int virtualPortsAmount) {
    this.virtualPortsAmount = virtualPortsAmount;
  }

  public final int getReservationsAmount() {
    return reservationsAmount;
  }

  public final void setReservationsAmount(int reservationsAmount) {
    this.reservationsAmount = reservationsAmount;
  }

  public final Long getId() {
    return id;
  }

  public final Institute getInstitute() {
    return institute;
  }

  public final String getAdminGroup() {
    return adminGroup;
  }

  public final String getManagerEmail() {
    return managerEmail;
  }

  public final Boolean getActive() {
    return active;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("PhysicalResourceGroupView [id=");
    builder.append(id);
    builder.append(", institute=");
    builder.append(institute);
    builder.append(", adminGroup=");
    builder.append(adminGroup);
    builder.append(", managerEmail=");
    builder.append(managerEmail);
    builder.append(", active=");
    builder.append(active);
    builder.append(", physicalPortsAmount=");
    builder.append(physicalPortsAmount);
    builder.append(", virtualPortsAmount=");
    builder.append(virtualPortsAmount);
    builder.append(", reservationsAmount=");
    builder.append(reservationsAmount);
    builder.append("]");
    return builder.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((active == null) ? 0 : active.hashCode());
    result = prime * result + ((adminGroup == null) ? 0 : adminGroup.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((institute == null) ? 0 : institute.hashCode());
    result = prime * result + ((managerEmail == null) ? 0 : managerEmail.hashCode());
    result = prime * result + physicalPortsAmount;
    result = prime * result + reservationsAmount;
    result = prime * result + virtualPortsAmount;
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
    PhysicalResourceGroupView other = (PhysicalResourceGroupView) obj;
    if (active == null) {
      if (other.active != null) {
        return false;
      }
    }
    else if (!active.equals(other.active)) {
      return false;
    }
    if (adminGroup == null) {
      if (other.adminGroup != null) {
        return false;
      }
    }
    else if (!adminGroup.equals(other.adminGroup)) {
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
    if (institute == null) {
      if (other.institute != null) {
        return false;
      }
    }
    else if (!institute.equals(other.institute)) {
      return false;
    }
    if (managerEmail == null) {
      if (other.managerEmail != null) {
        return false;
      }
    }
    else if (!managerEmail.equals(other.managerEmail)) {
      return false;
    }
    if (physicalPortsAmount != other.physicalPortsAmount) {
      return false;
    }
    if (reservationsAmount != other.reservationsAmount) {
      return false;
    }
    if (virtualPortsAmount != other.virtualPortsAmount) {
      return false;
    }
    return true;
  }

}
