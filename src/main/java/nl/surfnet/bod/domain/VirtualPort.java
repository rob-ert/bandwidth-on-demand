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

import javax.persistence.*;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Objects;

/**
 * Entity which represents a VirtualPort which is mapped to a
 * {@link PhysicalPort} and is related to a {@link VirtualResourceGroup}
 * 
 * @author Franky
 * 
 */
@Entity
@JsonIgnoreProperties({ "virtualResourceGroup" })
public class VirtualPort {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  @NotEmpty
  @Column(unique = true, nullable = false)
  private String name;

  @ManyToOne
  private VirtualResourceGroup virtualResourceGroup;

  @ManyToOne
  private PhysicalPort physicalPort;

  private Integer maxBandwidth;

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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("id", getId())
        .add("name", getName())
        .add("MaxBandwidth: ", getMaxBandwidth())
        .add("physicalPort", physicalPort).toString();
  }
}
