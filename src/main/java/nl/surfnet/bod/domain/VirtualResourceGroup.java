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
 * @author Franky
 *
 */
@Entity
public class VirtualResourceGroup {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  @NotEmpty
  @Column(unique = true, nullable = false)
  private String name;

  @Column(unique = true, nullable = false)
  private String surfConnextGroupName;

  @OneToMany(mappedBy = "virtualResourceGroup")
  private Collection<VirtualPort> virtualPorts;

  @OneToMany(mappedBy = "virtualResourceGroup")
  private Collection<Reservation> reservations;

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

  public String getSurfConnextGroupName() {
    return surfConnextGroupName;
  }

  public void setSurfConnextGroupName(String surfConnextGroupName) {
    this.surfConnextGroupName = surfConnextGroupName;
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

  @Override
  public String toString() {

    return name;
  }
}
