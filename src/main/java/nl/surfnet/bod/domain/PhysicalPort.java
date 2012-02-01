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
import javax.persistence.*;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

@Entity
@JsonIgnoreProperties({ "physicalResourceGroup" })
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

  @Nullable
  @Column(unique = true, nullable = false)
  private String networkElementPk;

  @ManyToOne
  private PhysicalResourceGroup physicalResourceGroup;

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

  public String getNetworkElementPk() {
    return networkElementPk;
  }

  public void setNetworkElementPk(String networkElementPk) {
    this.networkElementPk = networkElementPk;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(PhysicalPort.class)
        .add("id", id)
        .add("nocLabel", nocLabel)
        .add("managerLabel", managerLabel)
        .add("networkElementPk", networkElementPk)
        .add("physicalResourceGroup", physicalResourceGroup)
        .toString();
  }

  public String getManagerLabel() {
    return Strings.emptyToNull(managerLabel) == null ? nocLabel : managerLabel;
  }

  public void setManagerLabel(String managerLabel) {
    this.managerLabel = managerLabel;
  }

}
