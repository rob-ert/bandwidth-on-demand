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

import org.hibernate.validator.constraints.NotEmpty;

@Entity
public class PhysicalResourceGroup {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  @NotEmpty
  @Column(unique = true, nullable = false)
  private String name;

  /**
   * Institute is managed by IDD, we only persist the id of an {@link Institute}
   */
  @NotNull
  @Column(nullable = false)
  private Long instituteId;

  @Transient
  private Institute institute;

  private String adminGroup;

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

  public String getName() {
    return this.name;
  }

  public void setName(final String name) {
    this.name = name;
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

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Id: ").append(getId()).append(", ");
    sb.append("Name: ").append(getName()).append(", ");
    sb.append("InstitutionId: ").append(getInstituteId()).append(", ");
    sb.append("Admin group: ").append(getAdminGroup()).append(", ");
    sb.append("Version: ").append(getVersion());

    return sb.toString();
  }

  public Institute getInstitute() {
    return institute;
  }

  public void setInstitute(Institute institute) {
    this.instituteId = institute == null ? null : institute.getId();
    this.institute = institute;
  }
}
