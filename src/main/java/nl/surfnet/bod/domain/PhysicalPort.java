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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Strings;

@Indexed
@Entity
public class PhysicalPort {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  @Field
  @NotEmpty
  private String nocLabel;

  @Field
  private String managerLabel;

  @Field
  @NotEmpty
  private String bodPortId;

  @Field
  @NotEmpty
  @Column(unique = true)
  private String nmsPortId;

  @NotNull
  @ManyToOne(optional = false)
  private PhysicalResourceGroup physicalResourceGroup;

  @Basic
  private boolean vlanRequired;

  @Basic
  @Column(name = "aligned_nms")
  private boolean alignedWithNMS;

  @Field
  @Basic
  private String nmsNeId;

  @Field
  @Basic
  private String nmsPortSpeed;

  @Field
  @Basic
  private String nmsSapName;

  @Field
  @Basic
  private String signalingType;

  @Field
  @Basic
  private String supportedServiceType;

  public PhysicalPort() {
    this(false);
  }

  public PhysicalPort(boolean vlanRequired) {
    this.vlanRequired = vlanRequired;
    this.alignedWithNMS = true;
  }

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

  public String getNocLabel() {
    return this.nocLabel;
  }

  public void setNocLabel(final String name) {
    this.nocLabel = name;
  }

  public PhysicalResourceGroup getPhysicalResourceGroup() {
    return this.physicalResourceGroup;
  }

  public void setPhysicalResourceGroup(final PhysicalResourceGroup physicalResourceGroup) {
    this.physicalResourceGroup = physicalResourceGroup;
  }

  public String getNmsPortId() {
    return nmsPortId;
  }

  public void setNmsPortId(String nmsPortId) {
    this.nmsPortId = nmsPortId;
  }

  public String getManagerLabel() {
    return Strings.emptyToNull(managerLabel) == null ? nocLabel : managerLabel;
  }

  public boolean hasManagerLabel() {
    return Strings.emptyToNull(managerLabel) != null;
  }

  public void setManagerLabel(String managerLabel) {
    this.managerLabel = managerLabel;
  }

  public boolean isAllocated() {
    return getPhysicalResourceGroup() != null;
  }

  public String getBodPortId() {
    return bodPortId;
  }

  public void setBodPortId(String portId) {
    this.bodPortId = portId;
  }

  public boolean isVlanRequired() {
    return vlanRequired;
  }

  public void setAlignedWithNMS(boolean aligned) {
    this.alignedWithNMS = aligned;
  }

  public boolean isAlignedWithNMS() {
    return alignedWithNMS;
  }

  public String getNmsNeId() {
    return nmsNeId;
  }

  public void setNmsNeId(String nmsNeId) {
    this.nmsNeId = nmsNeId;
  }

  public String getNmsPortSpeed() {
    return nmsPortSpeed;
  }

  public void setNmsPortSpeed(String nmsPortSpeed) {
    this.nmsPortSpeed = nmsPortSpeed;
  }

  public String getNmsSapName() {
    return nmsSapName;
  }

  public void setNmsSapName(String nmsSapName) {
    this.nmsSapName = nmsSapName;
  }

  public final String getSignalingType() {
    return signalingType;
  }

  public final void setSignalingType(String signalingType) {
    this.signalingType = signalingType;
  }

  public final String getSupportedServiceType() {
    return supportedServiceType;
  }

  public final void setSupportedServiceType(String supportedServiceType) {
    this.supportedServiceType = supportedServiceType;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("PhysicalPort [");
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
    if (nocLabel != null) {
      builder.append("nocLabel=");
      builder.append(nocLabel);
      builder.append(", ");
    }
    if (managerLabel != null) {
      builder.append("managerLabel=");
      builder.append(managerLabel);
      builder.append(", ");
    }
    if (bodPortId != null) {
      builder.append("bodPortId=");
      builder.append(bodPortId);
      builder.append(", ");
    }
    if (nmsPortId != null) {
      builder.append("nmsPortId=");
      builder.append(nmsPortId);
      builder.append(", ");
    }
    if (physicalResourceGroup != null) {
      builder.append("physicalResourceGroup=");
      builder.append(physicalResourceGroup.getId());
      builder.append(", ");
    }
    if (nmsNeId != null) {
      builder.append("nmsNeId=");
      builder.append(nmsNeId);
      builder.append(", ");
    }
    if (nmsPortSpeed != null) {
      builder.append("nmsPortSpeed=");
      builder.append(nmsPortSpeed);
      builder.append(", ");
    }
    if (nmsSapName != null) {
      builder.append("nmsSapName=");
      builder.append(nmsSapName);
      builder.append(", ");
    }

    if (signalingType != null) {
      builder.append("signalingType=");
      builder.append(signalingType);
      builder.append(", ");
    }
    if (supportedServiceType != null) {
      builder.append("supportedServiceType=");
      builder.append(supportedServiceType);
      builder.append(", ");
    }
    builder.append("vlanRequired=");
    builder.append(vlanRequired);
    builder.append(", alignedWithNMS=");
    builder.append(alignedWithNMS);
    builder.append("]");
    return builder.toString();
  }

}
