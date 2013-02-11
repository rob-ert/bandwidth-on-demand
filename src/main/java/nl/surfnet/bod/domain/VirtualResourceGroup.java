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

import java.util.Collection;

import javax.persistence.*;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.ImmutableList;

/**
 * Entity which represents a List of {@link VirtualPort}s which belong together
 * and to the {@link Reservation}s which are related to this group.
 *
 */
@Entity
@Indexed
@Analyzer(definition = "customanalyzer")
public class VirtualResourceGroup implements Loggable, PersistableDomain {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @DocumentId
  private Long id;

  @Version
  private Integer version;

  @Field
  @NotEmpty
  @Column(nullable = false)
  private String name;

  @Field
  @Basic
  private String description;

  @Field
  @NotEmpty
  @Column(unique = true, nullable = false)
  private String adminGroup;

  @OneToMany(mappedBy = "virtualResourceGroup", cascade = CascadeType.REMOVE)
  @JsonIgnore
  private Collection<VirtualPort> virtualPorts;

  @OneToMany(mappedBy = "virtualResourceGroup", cascade = CascadeType.REMOVE)
  @JsonIgnore
  private Collection<Reservation> reservations;

  @OneToMany(mappedBy = "virtualResourceGroup", cascade = CascadeType.REMOVE)
  @JsonIgnore
  private Collection<VirtualPortRequestLink> virtualPortRequestLinks;

  @Override
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public Collection<VirtualPort> getVirtualPorts() {
    return virtualPorts;
  }

  public void setVirtualPorts(Collection<VirtualPort> virtualPorts) {
    this.virtualPorts = virtualPorts;
  }

  public boolean addVirtualPort(VirtualPort port) {
    return virtualPorts.add(port);
  }

  public Collection<Reservation> getReservations() {
    return reservations;
  }

  public void setReservations(Collection<Reservation> reservations) {
    this.reservations = reservations;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getVirtualPortCount() {
    return virtualPorts.size();
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Collection<VirtualPortRequestLink> getVirtualPortRequestLinks() {
    return virtualPortRequestLinks;
  }

  public void setVirtualPortRequestLinks(Collection<VirtualPortRequestLink> virtualPortRequestLinks) {
    this.virtualPortRequestLinks = virtualPortRequestLinks;
  }

  public boolean removeVirtualPort(VirtualPort port) {
    return this.virtualPorts.remove(port);
  }

  public String getAdminGroup() {
    return adminGroup;
  }

  public void setAdminGroup(String adminGroup) {
    this.adminGroup = adminGroup;
  }

  @Override
  public Collection<String> getAdminGroups() {
    return ImmutableList.of(adminGroup);
  }

  @Override
  public String getLabel() {
    return getName();
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
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    VirtualResourceGroup other = (VirtualResourceGroup) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    }
    else if (!id.equals(other.id)) {
      return false;
    }
    if (version == null) {
      if (other.version != null) {
        return false;
      }
    }
    else if (!version.equals(other.version)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("VirtualResourceGroup [");
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
    if (name != null) {
      builder.append("name=");
      builder.append(name);
      builder.append(", ");
    }
    if (description != null) {
      builder.append("description=");
      builder.append(description);
      builder.append(", ");
    }
    if (adminGroup != null) {
      builder.append("adminGroup=");
      builder.append(adminGroup);
      builder.append(", ");
    }
    if (virtualPorts != null) {
      builder.append("virtualPorts=[");
      for (VirtualPort vp : virtualPorts) {
        builder.append("mgrLabel: ").append(vp.getManagerLabel());
        builder.append(", ");
      }
      builder.append("], ");
    }
    if (reservations != null) {
      builder.append("reservations=[");
      for (Reservation res : reservations) {
        builder.append("SchId: ").append(res.getReservationId());
        builder.append(", ");
      }
      builder.append("], ");
    }
    if (virtualPortRequestLinks != null) {
      builder.append("virtualPortRequestLinks=[");
      for (VirtualPortRequestLink link : virtualPortRequestLinks) {
        builder.append("Link for: ").append(link.getVirtualResourceGroup().getName());
      }
      builder.append("]");
    }
    builder.append("]");

    return builder.toString();
  }
}
