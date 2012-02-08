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

import java.util.Collection;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
public class PhysicalResourceGroup {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  /**
   * Institute is managed by IDD, we only persist the id of an {@link Institute}
   */
  @NotNull
  @Column(nullable = false, unique = true)
  private Long instituteId;

  @Transient
  private Institute institute;

  @NotEmpty
  private String adminGroup;

  @NotEmpty
  @Email(message = "Not a valid email address")
  private String managerEmail;

  private Boolean active = false;

  @OneToMany(mappedBy = "physicalResourceGroup", cascade = CascadeType.REMOVE)
  private Collection<PhysicalPort> physicalPorts;

  public Long getId() {
    return this.id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public Integer getVersion() {
    return this.version;
  }

  public void setVersion(final Integer version) {
    this.version = version;
  }

  /**
   * This class will not have a name, instead it has a one-one relation to an
   * {@link Institute} and we will use the name of an Institute instead.
   * Whenever an instittute is not availabled (e.g. it was deleted in the IDD
   * system) the id of institute will be shown. This will trigger a NOC engineer
   * to investigate and correct this.
   *
   * @return Name of the related institute when available, the
   *         {@link #instituteId} otherwise.
   */
  public String getName() {
    return institute != null ? institute.getName() : String.valueOf(instituteId);
  }

  public Long getInstituteId() {
    return instituteId;
  }

  public void setInstituteId(Long instituteId) {
    this.institute = null;
    this.instituteId = instituteId;
  }

  public String getAdminGroup() {
    return adminGroup;
  }

  public void setAdminGroup(String adminGroup) {
    this.adminGroup = adminGroup;
  }

  public Collection<PhysicalPort> getPhysicalPorts() {
    return physicalPorts;
  }

  public void setPhysicalPorts(List<PhysicalPort> physicalPorts) {
    this.physicalPorts = physicalPorts;
  }

  public int getPhysicalPortCount() {
    return physicalPorts.size();
  }

  public String getManagerEmail() {
    return managerEmail;
  }

  public void setManagerEmail(String managerEmail) {
    this.managerEmail = managerEmail;
  }

  public Institute getInstitute() {
    return institute;
  }

  public void setInstitute(Institute institute) {
    this.instituteId = institute == null ? null : institute.getId();
    this.institute = institute;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public boolean isActive() {
    return active;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Id: ").append(getId()).append(", ");
    sb.append("InstituteId: ").append(getInstituteId()).append(", ");
    sb.append("Admin group: ").append(getAdminGroup()).append(", ");
    sb.append("Manager email: ").append(getManagerEmail()).append(", ");
    sb.append("Active: ").append(isActive()).append(", ");
    sb.append("Version: ").append(getVersion());

    return sb.toString();
  }
}
