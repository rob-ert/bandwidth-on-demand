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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

import com.google.common.base.Strings;

import nl.surfnet.bod.nsi.ws.ConnectionServiceProviderConstants;

/**
 * Entity which represents a VirtualPort which is mapped to a
 * {@link PhysicalPort} and is related to a {@link VirtualResourceGroup}
 *
 */
@Entity
public class VirtualPort {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  @NotEmpty
  @Column(nullable = false)
  private String managerLabel;

  @Column
  private String userLabel;

  @NotNull
  @ManyToOne(optional = false)
  private VirtualResourceGroup virtualResourceGroup;

  @NotNull
  @ManyToOne(optional = false)
  private PhysicalPort physicalPort;

  @NotNull
  @Column(nullable = false)
  private Integer maxBandwidth;

  @Range(min = 1, max = 4095)
  private Integer vlanId;

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
    return ConnectionServiceProviderConstants.URN_STP + ":" + getId();
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

}
