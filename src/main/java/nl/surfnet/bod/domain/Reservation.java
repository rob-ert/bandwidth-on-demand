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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import com.google.common.base.Objects;

/**
 * Entity which represents a Reservation for a specific connection between a
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
  private ReservationStatus status = ReservationStatus.REQUESTED;

  private String failedMessage;

  @NotNull
  @ManyToOne(optional = false)
  private VirtualPort sourcePort;

  @NotNull
  @ManyToOne(optional = false)
  private VirtualPort destinationPort;

  @Column(nullable = true)
  @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalDate")
  private LocalDate startDate;

  @Column(nullable = true)
  @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalTimeAsTime")
  private LocalTime startTime;

  @NotNull
  @Column(nullable = false)
  @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalDate")
  private LocalDate endDate;

  @NotNull
  @Column(nullable = false)
  @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalTimeAsTime")
  private LocalTime endTime;

  @Column(nullable = false)
  private String userCreated;

  @NotNull
  @Column(nullable = false)
  private Integer bandwidth;

  private String reservationId;

  @NotNull
  @Column(nullable = false)
  @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalDateTime")
  private LocalDateTime creationDateTime;

  public Reservation() {
    creationDateTime = LocalDateTime.now();
  }

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

  public ReservationStatus getStatus() {
    return status;
  }

  public void setStatus(ReservationStatus reservationStatus) {
    this.status = reservationStatus;
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

  public void setDestinationPort(VirtualPort destinationPort) {
    this.destinationPort = destinationPort;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalTime startTime) {
    this.startTime = startTime;
  }

  public String getUserCreated() {
    return userCreated;
  }

  public void setUserCreated(String user) {
    this.userCreated = user;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalTime endTime) {
    this.endTime = endTime;
  }

  public LocalDateTime getEndDateTime() {
    return endDate.toLocalDateTime(endTime);
  }

  public LocalDateTime getStartDateTime() {
    return startDate == null ? null : startDate.toLocalDateTime(startTime);
  }

  public Integer getBandwidth() {
    return bandwidth;
  }

  public void setBandwidth(Integer bandwidth) {
    this.bandwidth = bandwidth;
  }

  public String getReservationId() {
    return reservationId;
  }

  public void setReservationId(String reservationId) {
    this.reservationId = reservationId;
  }

  public LocalDateTime getCreationDateTime() {
    return creationDateTime;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(Reservation.class).add("id", id).add("startDate", startDate)
        .add("starTime", startTime).add("endDate", endDate).add("endTime", endTime).add("sourcePort", sourcePort)
        .add("destinationPort", destinationPort).add("userCreated", userCreated)
        .add("creationDT", creationDateTime).toString();
  }

  public String getFailedMessage() {
    return failedMessage;
  }

  public void setFailedMessage(String failedMessage) {
    this.failedMessage = failedMessage;
  }

}
