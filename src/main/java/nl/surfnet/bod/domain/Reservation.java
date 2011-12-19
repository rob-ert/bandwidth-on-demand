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

import java.util.Date;

import javax.persistence.*;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * Entity which represents a Reserveration for a specific connection between a
 * source and a destination point on a specific moment in time.
 *
 * @author Franky
 *
 */
@Entity
public class Reservation {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Version
  private Integer version;

  @ManyToOne
  private VirtualResourceGroup virtualResourceGroup;

  @Enumerated(EnumType.STRING)
  private ReservationStatus reservationStatus;

  @ManyToOne
  private VirtualPort sourcePort;

  @ManyToOne
  private VirtualPort destinationPort;

  @DateTimeFormat(style = "S-")
  @Temporal(TemporalType.DATE)
  @Column(nullable = false)
  private Date startDate;

  @DateTimeFormat(style = "-S")
  @Temporal(TemporalType.TIME)
  @Column(nullable = false)
  private Date startTime;

  @DateTimeFormat(style = "S-")
  @Temporal(TemporalType.DATE)
  @Column(nullable = false)
  private Date endDate;

  @DateTimeFormat(style = "-S")
  @Temporal(TemporalType.TIME)
  @Column(nullable = false)
  private Date endTime;

  @Column(nullable = false)
  private String user;

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

  public VirtualResourceGroup getVirtualResourceGroup() {
    return virtualResourceGroup;
  }

  public void setVirtualResourceGroup(VirtualResourceGroup virtualResourceGroup) {
    this.virtualResourceGroup = virtualResourceGroup;
  }

  public ReservationStatus getReservationStatus() {
    return reservationStatus;
  }

  public void setReservationStatus(ReservationStatus reservationStatus) {
    this.reservationStatus = reservationStatus;
  }

  public VirtualPort getSourcePort() {
    return sourcePort;
  }

  public void setSourcePort(VirtualPort sourcePort) {
    this.sourcePort = sourcePort;
  }

  public VirtualPort getDestinationPort() {
    return destinationPort;
  }

  public void setDestinationPort(VirtualPort endPort) {
    this.destinationPort = endPort;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  /**
   * TODO Convenience method since bean notation is not allowed in view
   *
   */
  public String getVirtualResourceGroupName() {
    return this.virtualResourceGroup == null ? "" : this.virtualResourceGroup.getName();
  }

  public String getSourcePortName() {
    return this.getSourcePort() == null ? "" : this.sourcePort.getName();
  }

  public String getDestinationPortName() {
    return this.getDestinationPort() == null ? "" : this.getDestinationPort().getName();
  }
}
