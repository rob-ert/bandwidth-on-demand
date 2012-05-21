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
package nl.surfnet.bod.web.view;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.LocalDateTime;

import com.google.common.base.Objects;

public class ReservationView {
  private final Long id;
  private final String name;
  private final String virtualResourceGroup;
  private final String sourcePort;
  private final String destinationPort;
  private final ReservationStatus status;
  private final String failedMessage;
  @JsonSerialize(using = JsonLocalDateTimeSerializer.class)
  private final LocalDateTime startDateTime;
  @JsonSerialize(using = JsonLocalDateTimeSerializer.class)
  private final LocalDateTime endDateTime;
  private final Integer bandwidth;
  private final String userCreated;
  private final String reservationId;
  private final LocalDateTime creationDateTime;
  private final ElementActionView deleteActionView;

  public ReservationView(final Reservation reservation, final ElementActionView deleteActionView) {
    this.id = reservation.getId();
    this.virtualResourceGroup = reservation.getVirtualResourceGroup().getName();
    this.sourcePort = reservation.getSourcePort().getUserLabel();
    this.destinationPort = reservation.getDestinationPort().getUserLabel();
    this.status = reservation.getStatus();
    this.failedMessage = reservation.getFailedMessage();
    this.startDateTime = reservation.getStartDateTime();
    this.endDateTime = reservation.getEndDateTime();
    this.bandwidth = reservation.getBandwidth();
    this.userCreated = reservation.getUserCreated();
    this.reservationId = reservation.getReservationId();
    this.creationDateTime = reservation.getCreationDateTime();
    this.name = reservation.getName();
    this.deleteActionView = deleteActionView;
  }

  public String getVirtualResourceGroup() {
    return virtualResourceGroup;
  }

  public String getSourcePort() {
    return sourcePort;
  }

  public String getDestinationPort() {
    return destinationPort;
  }

  public ReservationStatus getStatus() {
    return status;
  }

  public String getFailedMessage() {
    return failedMessage;
  }

  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  public Integer getBandwidth() {
    return bandwidth;
  }

  public Long getId() {
    return id;
  }

  public String getUserCreated() {
    return userCreated;
  }

  public String getReservationId() {
    return reservationId;
  }

  public LocalDateTime getCreationDateTime() {
    return creationDateTime;
  }

  public String getName() {
    return name;
  }

  public boolean isDeleteAllowedForSelectedRole() {
    return deleteActionView.isAllowed();
  }

  public String getDeleteReasonKey() {
    return deleteActionView.getReasonKey();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj instanceof ReservationView) {
      ReservationView resView = (ReservationView) obj;

      return Objects.equal(this.id, resView.id)
          && Objects.equal(this.virtualResourceGroup, resView.virtualResourceGroup)
          && Objects.equal(this.status, resView.status) && Objects.equal(this.failedMessage, resView.failedMessage)
          && Objects.equal(this.sourcePort, resView.sourcePort)
          && Objects.equal(this.destinationPort, resView.destinationPort)
          && Objects.equal(this.startDateTime, resView.startDateTime)
          && Objects.equal(this.endDateTime, resView.endDateTime)
          && Objects.equal(this.userCreated, resView.userCreated) && Objects.equal(this.bandwidth, resView.bandwidth)
          && Objects.equal(this.creationDateTime, resView.creationDateTime)
          && Objects.equal(this.reservationId, resView.reservationId) && Objects.equal(this.name, resView.name);
    }
    else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, virtualResourceGroup, sourcePort, destinationPort, status, failedMessage,
        startDateTime, endDateTime, bandwidth, userCreated, reservationId, creationDateTime, name);
  }

}