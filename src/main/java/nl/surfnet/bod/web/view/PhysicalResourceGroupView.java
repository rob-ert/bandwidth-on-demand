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
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PhysicalResourceGroupView other = (PhysicalResourceGroupView) obj;
    if (active == null) {
      if (other.active != null)
        return false;
    }
    else if (!active.equals(other.active))
      return false;
    if (adminGroup == null) {
      if (other.adminGroup != null)
        return false;
    }
    else if (!adminGroup.equals(other.adminGroup))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    if (institute == null) {
      if (other.institute != null)
        return false;
    }
    else if (!institute.equals(other.institute))
      return false;
    if (managerEmail == null) {
      if (other.managerEmail != null)
        return false;
    }
    else if (!managerEmail.equals(other.managerEmail))
      return false;
    if (physicalPortsAmount != other.physicalPortsAmount)
      return false;
    if (reservationsAmount != other.reservationsAmount)
      return false;
    if (virtualPortsAmount != other.virtualPortsAmount)
      return false;
    return true;
  }

}
