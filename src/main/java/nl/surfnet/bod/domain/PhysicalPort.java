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

import javax.annotation.Nullable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Strings;

@Entity
public class PhysicalPort {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  @NotEmpty
  @Column(name = "noc_label", nullable = false)
  private String nocLabel;

  private String managerLabel;

  @NotEmpty
  @Column(nullable = false)
  private String bodPortId;

  @Nullable
  @Column(unique = true, nullable = false)
  private String nmsPortId;

  @ManyToOne
  private PhysicalResourceGroup physicalResourceGroup;

  @Basic
  private boolean vlanRequired;

  @Basic
  @Column(name = "aligned_nms")
  private boolean alignedWithNMS;

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

  @Override
  public String toString() {
    return "PhysicalPort [id=" + id + ", version=" + version + ", nocLabel=" + nocLabel + ", managerLabel="
        + managerLabel + ", bodPortId=" + bodPortId + ", nmsPortId=" + nmsPortId + ", physicalResourceGroup="
        + physicalResourceGroup + ", vlanRequired=" + vlanRequired + ", alignedWithNMS=" + alignedWithNMS + "]";
  }

}
