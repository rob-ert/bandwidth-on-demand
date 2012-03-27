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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
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
  @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalDateTime")
  private LocalDateTime startDateTime;

  @NotNull
  @Column(nullable = false)
  @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalDateTime")
  private LocalDateTime endDateTime;

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
    return startDateTime == null ? null : startDateTime.toLocalTime();
  }

  public void setStartTime(LocalTime startTime) {

    if (startTime == null) {
      startDateTime = null;
      return;
    }

    if (startDateTime == null) {
      startDateTime = new LocalDateTime(startTime);
    }
    else {
      startDateTime = startDateTime.withTime(startTime.getHourOfDay(), startTime.getMinuteOfHour(),
          startTime.getSecondOfMinute(), startTime.getMillisOfSecond());
    }
  }

  public String getUserCreated() {
    return userCreated;
  }

  public void setUserCreated(String user) {
    this.userCreated = user;
  }

  public LocalDate getStartDate() {
    return startDateTime == null ? null : startDateTime.toLocalDate();
  }

  public void setStartDate(LocalDate startDate) {

    if (startDate == null) {
      startDateTime = null;
      return;
    }

    if (startDateTime == null) {
      startDateTime = new LocalDateTime(startDate.toDate());
    }
    else {
      startDateTime = startDateTime
          .withDate(startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth());
    }
  }

  public LocalDate getEndDate() {
    return endDateTime == null ? null : endDateTime.toLocalDate();
  }

  public void setEndDate(LocalDate endDate) {

    if (endDate == null) {
      endDateTime = null;
      return;
    }

    if (endDateTime == null) {
      this.endDateTime = new LocalDateTime(endDate.toDate());
    }
    else {
      endDateTime = endDateTime.withDate(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth());
    }
  }

  public LocalTime getEndTime() {
    return endDateTime == null ? null : endDateTime.toLocalTime();
  }

  public void setEndTime(LocalTime endTime) {

    if (endTime == null) {
      endDateTime = null;
      return;
    }

    if (endDateTime == null) {
      this.endDateTime = new LocalDateTime(endTime);
    }
    else {
      endDateTime = endDateTime.withTime(endTime.getHourOfDay(), endTime.getMinuteOfHour(),
          endTime.getSecondOfMinute(), endTime.getMillisOfSecond());
    }
  }

  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  public LocalDateTime getStartDateTime() {
    return startDateTime;
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
    return Objects.toStringHelper(Reservation.class).add("id", id).add("startDateTime", startDateTime)
        .add("endDateTime", endDateTime).add("sourcePort", sourcePort).add("destinationPort", destinationPort)
        .add("userCreated", userCreated).add("creationDT", creationDateTime).toString();
  }

  public String getFailedMessage() {
    return failedMessage;
  }

  public void setFailedMessage(String failedMessage) {
    this.failedMessage = failedMessage;
  }

}
