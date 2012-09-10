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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

@Indexed
@Entity
public class PhysicalResourceGroup implements Loggable {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  @IndexedEmbedded
  @OneToOne(optional = false)
  @JoinColumn(name = "institute_id")
  private Institute institute;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @NotEmpty
  private String adminGroup;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @NotEmpty
  @Email(message = "Not a valid email address")
  private String managerEmail;

  @NotNull
  @Column(nullable = false)
  private Boolean active = false;

  @ContainedIn
  @OneToMany(mappedBy = "physicalResourceGroup", cascade = CascadeType.REMOVE)
  private Collection<PhysicalPort> physicalPorts;

  @OneToMany(mappedBy = "physicalResourceGroup", cascade = CascadeType.REMOVE)
  private Collection<VirtualPortRequestLink> virtualPortRequestLinks;

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
   * 
   * @return Name of the related institute
   */
  public String getName() {
    return institute != null ? institute.getName() : null;
  }

  @Override
  public String getAdminGroup() {
    return adminGroup;
  }

  @Override
  public String getLabel() {
    return institute.getLabel();
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
    StringBuilder builder = new StringBuilder();
    builder.append("PhysicalResourceGroup [");
    if (id != null) {
      builder.append("id=");
      builder.append(id);
      builder.append(", ");
    }
    if (version != null) {
      builder.append("version=");
      builder.append(version);
      builder.append(", ");
    }
    if (institute != null) {
      builder.append("institute=");
      builder.append(institute);
      builder.append(", ");
    }
    if (adminGroup != null) {
      builder.append("adminGroup=");
      builder.append(adminGroup);
      builder.append(", ");
    }
    if (managerEmail != null) {
      builder.append("managerEmail=");
      builder.append(managerEmail);
      builder.append(", ");
    }
    if (active != null) {
      builder.append("active=");
      builder.append(active);
      builder.append(", ");
    }
    if (physicalPorts != null) {
      builder.append("physicalPorts=");
      builder.append(physicalPorts);
      builder.append(", ");
    }
    if (virtualPortRequestLinks != null) {
      builder.append("virtualPortRequestLinks=");
      builder.append(virtualPortRequestLinks);
    }
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
    result = prime * result + ((physicalPorts == null) ? 0 : physicalPorts.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    result = prime * result + ((virtualPortRequestLinks == null) ? 0 : virtualPortRequestLinks.hashCode());
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
    PhysicalResourceGroup other = (PhysicalResourceGroup) obj;
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
    if (physicalPorts == null) {
      if (other.physicalPorts != null)
        return false;
    }
    else if (!physicalPorts.equals(other.physicalPorts))
      return false;
    if (version == null) {
      if (other.version != null)
        return false;
    }
    else if (!version.equals(other.version))
      return false;
    if (virtualPortRequestLinks == null) {
      if (other.virtualPortRequestLinks != null)
        return false;
    }
    else if (!virtualPortRequestLinks.equals(other.virtualPortRequestLinks))
      return false;
    return true;
  }
}
