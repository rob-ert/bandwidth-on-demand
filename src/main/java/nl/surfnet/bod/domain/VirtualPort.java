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

import static nl.surfnet.bod.nsi.NsiConstants.URN_STP;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.hibernate.search.annotations.*;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

import com.google.common.base.Strings;

/**
 * Entity which represents a VirtualPort which is mapped to a
 * {@link PhysicalPort} and is related to a {@link VirtualResourceGroup}
 *
 */
@Entity
@Indexed
public class VirtualPort implements Loggable, PersistableDomain {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @NotEmpty
  @Column(nullable = false)
  private String managerLabel;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @Column
  private String userLabel;

  @IndexedEmbedded
  @NotNull
  @ManyToOne(optional = false)
  private VirtualResourceGroup virtualResourceGroup;

  @IndexedEmbedded
  @NotNull
  @ManyToOne(optional = false)
  private PhysicalPort physicalPort;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @NotNull
  @Column(nullable = false)
  private Integer maxBandwidth;

  @Field(index = Index.YES, store = Store.YES)
  @Analyzer(definition = "customanalyzer")
  @Range(min = 1, max = 4095)
  private Integer vlanId;

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

  public String getManagerLabel() {
    return managerLabel;
  }

  public void setManagerLabel(String name) {
    this.managerLabel = name;
  }

  public VirtualResourceGroup getVirtualResourceGroup() {
    return virtualResourceGroup;
  }

  public void setVirtualResourceGroup(VirtualResourceGroup virtualResourceGroup) {
    this.virtualResourceGroup = virtualResourceGroup;
  }

  public PhysicalPort getPhysicalPort() {
    return physicalPort;
  }

  public void setPhysicalPort(PhysicalPort physicalPort) {
    this.physicalPort = physicalPort;
  }

  public PhysicalResourceGroup getPhysicalResourceGroup() {
    return physicalPort == null ? null : physicalPort.getPhysicalResourceGroup();
  }

  public Integer getMaxBandwidth() {
    return maxBandwidth;
  }

  public void setMaxBandwidth(Integer maxBandwidth) {
    this.maxBandwidth = maxBandwidth;
  }

  public Integer getVlanId() {
    return vlanId;
  }

  public void setVlanId(Integer vlanId) {
    this.vlanId = vlanId;
  }

  public String getUserLabel() {
    return Strings.emptyToNull(userLabel) == null ? managerLabel : userLabel;
  }

  public void setUserLabel(String userLabel) {
    this.userLabel = userLabel;
  }

  public String getNsiStpId() {
    return URN_STP + ":" + getId();
  }

  @Override
  public String getAdminGroup() {
    return virtualResourceGroup.getAdminGroup();
  }

  @Override
  public String getLabel() {
    return getManagerLabel();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("VirtualPort [");
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
    if (managerLabel != null) {
      builder.append("managerLabel=");
      builder.append(managerLabel);
      builder.append(", ");
    }
    if (userLabel != null) {
      builder.append("userLabel=");
      builder.append(userLabel);
      builder.append(", ");
    }
    if (virtualResourceGroup != null) {
      builder.append("virtualResourceGroup=");
      builder.append(virtualResourceGroup.getName());
      builder.append(", ");
    }
    if (physicalPort != null) {
      builder.append("physicalPort=");
      builder.append(physicalPort);
      builder.append(", ");
    }
    if (maxBandwidth != null) {
      builder.append("maxBandwidth=");
      builder.append(maxBandwidth);
      builder.append(", ");
    }
    if (vlanId != null) {
      builder.append("vlanId=");
      builder.append(vlanId);
    }
    builder.append("]");
    return builder.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((managerLabel == null) ? 0 : managerLabel.hashCode());
    result = prime * result + ((maxBandwidth == null) ? 0 : maxBandwidth.hashCode());
    result = prime * result + ((physicalPort == null) ? 0 : physicalPort.hashCode());
    result = prime * result + ((userLabel == null) ? 0 : userLabel.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    result = prime * result + ((virtualResourceGroup == null) ? 0 : virtualResourceGroup.getLabel().hashCode());
    result = prime * result + ((vlanId == null) ? 0 : vlanId.hashCode());
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
    VirtualPort other = (VirtualPort) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    if (managerLabel == null) {
      if (other.managerLabel != null)
        return false;
    }
    else if (!managerLabel.equals(other.managerLabel))
      return false;
    if (maxBandwidth == null) {
      if (other.maxBandwidth != null)
        return false;
    }
    else if (!maxBandwidth.equals(other.maxBandwidth))
      return false;
    if (physicalPort == null) {
      if (other.physicalPort != null)
        return false;
    }
    else if (!physicalPort.equals(other.physicalPort))
      return false;
    if (userLabel == null) {
      if (other.userLabel != null)
        return false;
    }
    else if (!userLabel.equals(other.userLabel))
      return false;
    if (version == null) {
      if (other.version != null)
        return false;
    }
    else if (!version.equals(other.version))
      return false;
    if (virtualResourceGroup == null) {
      if (other.virtualResourceGroup != null)
        return false;
    }
    else if (!virtualResourceGroup.getLabel().equals(other.virtualResourceGroup.getLabel()))
      return false;
    if (vlanId == null) {
      if (other.vlanId != null)
        return false;
    }
    else if (!vlanId.equals(other.vlanId))
      return false;
    return true;
  }
}
