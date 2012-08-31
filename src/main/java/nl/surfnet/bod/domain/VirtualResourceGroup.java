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

import javax.persistence.*;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * Entity which represents a List of {@link VirtualPort}s which belong together
 * and to the {@link Reservation}s which are related to this group.
 *
 */
@Entity
public class VirtualResourceGroup implements Loggable {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  @NotEmpty
  @Column(nullable = false)
  private String name;

  @Basic
  private String description;

  @NotEmpty
  @Column(unique = true, nullable = false)
  private String surfconextGroupId;

  @OneToMany(mappedBy = "virtualResourceGroup", cascade = CascadeType.REMOVE)
  private Collection<VirtualPort> virtualPorts;

  @OneToMany(mappedBy = "virtualResourceGroup", cascade = CascadeType.REMOVE)
  private Collection<Reservation> reservations;

  @OneToMany(mappedBy = "virtualResourceGroup", cascade = CascadeType.REMOVE)
  private Collection<VirtualPortRequestLink> virtualPortRequestLinks;

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

  public String getSurfconextGroupId() {
    return surfconextGroupId;
  }

  public void setSurfconextGroupId(String surfconextGroupId) {
    this.surfconextGroupId = surfconextGroupId;
  }

  public Collection<VirtualPort> getVirtualPorts() {
    return virtualPorts;
  }

  public void setVirtualPorts(Collection<VirtualPort> virtualPorts) {
    this.virtualPorts = virtualPorts;
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((surfconextGroupId == null) ? 0 : surfconextGroupId.hashCode());
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
    VirtualResourceGroup other = (VirtualResourceGroup) obj;
    if (description == null) {
      if (other.description != null)
        return false;
    }
    else if (!description.equals(other.description))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    }
    else if (!name.equals(other.name))
      return false;
    if (surfconextGroupId == null) {
      if (other.surfconextGroupId != null)
        return false;
    }
    else if (!surfconextGroupId.equals(other.surfconextGroupId))
      return false;
    return true;
  }

  @Override
  public String getAdminGroup() {
    return surfconextGroupId;
  }

  @Override
  public String getLabel() {
    return getName();
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
    if (surfconextGroupId != null) {
      builder.append("surfconextGroupId=");
      builder.append(surfconextGroupId);
      builder.append(", ");
    }
//    if (virtualPorts != null) {
//      builder.append("virtualPorts=[");
//      for (VirtualPort vp : virtualPorts) {
//        builder.append("mgrLabel: ").append(vp.getManagerLabel());
//        builder.append(", ");
//      }
//      builder.append("]");
//    }
//    if (reservations != null) {
//      builder.append("reservations=");
//      for (Reservation res : reservations) {
//        builder.append("SchId: ").append(res.getReservationId());
//        builder.append(", ");
//      }
//      builder.append("]");
//    }
//    if (virtualPortRequestLinks != null) {
//      builder.append("virtualPortRequestLinks=[");
//      for (VirtualPortRequestLink link : virtualPortRequestLinks) {
//        builder.append("Link for: ").append(link.getVirtualResourceGroup().getName());
//      }
//      builder.append("]");
//    }
    builder.append("]");
    return builder.toString();
  }
}
