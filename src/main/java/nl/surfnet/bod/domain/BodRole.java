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
package nl.surfnet.bod.domain;

import java.util.concurrent.atomic.AtomicLong;

import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.security.Security.RoleEnum;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

/**
 * Represents a role so the user can switch between them. Note that only the
 * {@link #role} and {@link #instituteId} are relevant for the
 * {@link #equals(Object)} and {@link #hashCode()}. This way duplicate
 * {@link Security.RoleEnum#USER} are prevented even if the are related to
 * different groups. Multiple {@link RoleEnum#ICT_MANAGER} roles are allowed, as
 * long as they are related to different {@link #instituteId}s.
 * 
 */
public final class BodRole {

  private static final AtomicLong COUNTER = new AtomicLong();

  private final Long id;
  private final RoleEnum role;

  private final Optional<String> adminGroup;
  private final Optional<String> instituteName;
  private final Optional<Long> physicalResourceGroupId;

  private BodRole(Security.RoleEnum role) {
    this(role, null);
  }

  private BodRole(Security.RoleEnum role, PhysicalResourceGroup physicalResourceGroup) {
    this.role = role;
    this.id = COUNTER.incrementAndGet();

    this.instituteName = physicalResourceGroup == null ? Optional.<String> absent() : Optional.of(physicalResourceGroup
        .getName());
    this.physicalResourceGroupId = physicalResourceGroup == null ? Optional.<Long> absent() : Optional
        .of(physicalResourceGroup.getId());
    this.adminGroup = physicalResourceGroup == null ? Optional.<String> absent() : Optional.of(physicalResourceGroup
        .getAdminGroup());
  }

  public static BodRole createNewUser() {
    return new BodRole(RoleEnum.NEW_USER);
  }

  public static BodRole createUser() {
    return new BodRole(RoleEnum.USER);
  }

  public static BodRole createNocEngineer() {
    return new BodRole(RoleEnum.NOC_ENGINEER);
  }

  public static BodRole createManager(PhysicalResourceGroup prg) {
    return new BodRole(RoleEnum.ICT_MANAGER, prg);
  }

  public Long getId() {
    return id;
  }

  public Optional<String> getInstituteName() {
    return instituteName;
  }

  public Optional<Long> getPhysicalResourceGroupId() {
    return physicalResourceGroupId;
  }

  public Optional<String> getAdminGroup() {
    return adminGroup;
  }

  public String getRoleName() {
    return role.name();
  }

  public RoleEnum getRole() {
    return role;
  }

  public boolean isManagerRole() {
    return getRole() == RoleEnum.ICT_MANAGER;
  }

  public boolean isNocRole() {
    return getRole() == RoleEnum.NOC_ENGINEER;
  }

  public boolean isUserRole() {
    return getRole() == RoleEnum.USER;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("id", id).add("role", role).add("instituteName", instituteName)
        .add("physicalResourceGroupId", physicalResourceGroupId).toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((adminGroup == null) ? 0 : adminGroup.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((instituteName == null) ? 0 : instituteName.hashCode());
    result = prime * result + ((physicalResourceGroupId == null) ? 0 : physicalResourceGroupId.hashCode());
    result = prime * result + ((role == null) ? 0 : role.hashCode());
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
    BodRole other = (BodRole) obj;
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
    if (instituteName == null) {
      if (other.instituteName != null)
        return false;
    }
    else if (!instituteName.equals(other.instituteName))
      return false;
    if (physicalResourceGroupId == null) {
      if (other.physicalResourceGroupId != null)
        return false;
    }
    else if (!physicalResourceGroupId.equals(other.physicalResourceGroupId))
      return false;
    if (role != other.role)
      return false;
    return true;
  }
}
