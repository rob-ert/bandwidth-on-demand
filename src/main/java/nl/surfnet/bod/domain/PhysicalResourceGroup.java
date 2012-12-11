/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.ImmutableList;

@Indexed
@Entity
@Analyzer(definition = "customanalyzer")
public class PhysicalResourceGroup implements Loggable, PersistableDomain {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @DocumentId
  private Long id;

  @Version
  private Integer version;

  @IndexedEmbedded
  @OneToOne(optional = false)
  @JoinColumn(name = "institute_id")
  private Institute institute;

  @Field
  @NotEmpty
  private String adminGroup;

  @Field
  @NotEmpty
  @Email(message = "Not a valid email address")
  private String managerEmail;

  @NotNull
  @Column(nullable = false)
  private Boolean active = false;

  @ContainedIn
  @OneToMany(mappedBy = "physicalResourceGroup", cascade = CascadeType.REMOVE)
  @JsonIgnore
  private Collection<PhysicalPort> physicalPorts;

  @OneToMany(mappedBy = "physicalResourceGroup", cascade = CascadeType.REMOVE)
  @JsonIgnore
  private Collection<VirtualPortRequestLink> virtualPortRequestLinks;

  @Override
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

  public String getAdminGroup() {
    return adminGroup;
  }

  @Override
  public Collection<String> getAdminGroups() {
    return ImmutableList.of(adminGroup);
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
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
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
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    if (version == null) {
      if (other.version != null)
        return false;
    }
    else if (!version.equals(other.version))
      return false;
    return true;
  }
}
