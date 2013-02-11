/**
 * Copyright (c) 2012, SURFnet BV
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

  public static BodRole createAppManager() {
    return new BodRole(RoleEnum.APP_MANAGER);
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

  public boolean isAppManagerRole() {
    return getRole() == RoleEnum.APP_MANAGER;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("id", id).add("role", role).add("instituteName", instituteName).add(
        "physicalResourceGroupId", physicalResourceGroupId).toString();
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
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BodRole other = (BodRole) obj;
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
    if (instituteName == null) {
      if (other.instituteName != null) {
        return false;
      }
    }
    else if (!instituteName.equals(other.instituteName)) {
      return false;
    }
    if (physicalResourceGroupId == null) {
      if (other.physicalResourceGroupId != null) {
        return false;
      }
    }
    else if (!physicalResourceGroupId.equals(other.physicalResourceGroupId)) {
      return false;
    }
    if (role != other.role) {
      return false;
    }
    return true;
  }
}