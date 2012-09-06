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

import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.LocalDateTime;

public class ReservationView {
  private final Long id;
  private final String name;
  private final String virtualResourceGroup;
  private final ReservationStatus status;
  private final PortView sourcePort;
  private final PortView destinationPort;
  private final String failedReason;
  private final String cancelReason;
  @JsonSerialize(using = JsonLocalDateTimeSerializer.class)
  private final LocalDateTime startDateTime;
  @JsonSerialize(using = JsonLocalDateTimeSerializer.class)
  private final LocalDateTime endDateTime;
  private final Integer bandwidth;
  private final String userCreated;
  private final String reservationId;
  private final String connectionId;
  private final LocalDateTime creationDateTime;
  private final ElementActionView deleteActionView;
  private final ProtectionType protectionType;

  public ReservationView(final Reservation reservation, final ElementActionView deleteActionView) {
    this.id = reservation.getId();
    this.virtualResourceGroup = reservation.getVirtualResourceGroup().getName();
    this.sourcePort = new PortView(reservation.getSourcePort());
    this.destinationPort = new PortView(reservation.getDestinationPort());
    this.status = reservation.getStatus();
    this.failedReason = reservation.getFailedReason();
    this.cancelReason = reservation.getCancelReason();
    this.startDateTime = reservation.getStartDateTime();
    this.endDateTime = reservation.getEndDateTime();
    this.bandwidth = reservation.getBandwidth();
    this.userCreated = reservation.getUserCreated();
    this.reservationId = reservation.getReservationId();
    this.creationDateTime = reservation.getCreationDateTime();
    this.protectionType = reservation.getProtectionType();
    this.name = reservation.getName();
    this.deleteActionView = deleteActionView;
    this.connectionId = reservation.getConnection() == null ? null : reservation.getConnection().getConnectionId();
  }

  public String getVirtualResourceGroup() {
    return virtualResourceGroup;
  }

  public PortView getSourcePort() {
    return sourcePort;
  }

  public PortView getDestinationPort() {
    return destinationPort;
  }

  public ReservationStatus getStatus() {
    return status;
  }

  public String getFailedReason() {
    return failedReason;
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

  public String getConnectionId() {
    return connectionId;
  }

  public String getCancelReason() {
    return cancelReason;
  }

  public ProtectionType getProtectionType() {
    return protectionType;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((bandwidth == null) ? 0 : bandwidth.hashCode());
    result = prime * result + ((cancelReason == null) ? 0 : cancelReason.hashCode());
    result = prime * result + ((connectionId == null) ? 0 : connectionId.hashCode());
    result = prime * result + ((creationDateTime == null) ? 0 : creationDateTime.hashCode());
    result = prime * result + ((deleteActionView == null) ? 0 : deleteActionView.hashCode());
    result = prime * result + ((destinationPort == null) ? 0 : destinationPort.hashCode());
    result = prime * result + ((endDateTime == null) ? 0 : endDateTime.hashCode());
    result = prime * result + ((failedReason == null) ? 0 : failedReason.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((reservationId == null) ? 0 : reservationId.hashCode());
    result = prime * result + ((sourcePort == null) ? 0 : sourcePort.hashCode());
    result = prime * result + ((startDateTime == null) ? 0 : startDateTime.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    result = prime * result + ((userCreated == null) ? 0 : userCreated.hashCode());
    result = prime * result + ((virtualResourceGroup == null) ? 0 : virtualResourceGroup.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ReservationView other = (ReservationView) obj;
    if (bandwidth == null) {
      if (other.bandwidth != null)
        return false;
    }
    else if (!bandwidth.equals(other.bandwidth))
      return false;
    if (cancelReason == null) {
      if (other.cancelReason != null)
        return false;
    }
    else if (!cancelReason.equals(other.cancelReason))
      return false;
    if (connectionId == null) {
      if (other.connectionId != null)
        return false;
    }
    else if (!connectionId.equals(other.connectionId))
      return false;
    if (creationDateTime == null) {
      if (other.creationDateTime != null)
        return false;
    }
    else if (!creationDateTime.equals(other.creationDateTime))
      return false;
    if (deleteActionView == null) {
      if (other.deleteActionView != null)
        return false;
    }
    else if (!deleteActionView.equals(other.deleteActionView))
      return false;
    if (destinationPort == null) {
      if (other.destinationPort != null)
        return false;
    }
    else if (!destinationPort.equals(other.destinationPort))
      return false;
    if (endDateTime == null) {
      if (other.endDateTime != null)
        return false;
    }
    else if (!endDateTime.equals(other.endDateTime))
      return false;
    if (failedReason == null) {
      if (other.failedReason != null)
        return false;
    }
    else if (!failedReason.equals(other.failedReason))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    }
    else if (!name.equals(other.name))
      return false;
    if (reservationId == null) {
      if (other.reservationId != null)
        return false;
    }
    else if (!reservationId.equals(other.reservationId))
      return false;
    if (sourcePort == null) {
      if (other.sourcePort != null)
        return false;
    }
    else if (!sourcePort.equals(other.sourcePort))
      return false;
    if (startDateTime == null) {
      if (other.startDateTime != null)
        return false;
    }
    else if (!startDateTime.equals(other.startDateTime))
      return false;
    if (status != other.status)
      return false;
    if (userCreated == null) {
      if (other.userCreated != null)
        return false;
    }
    else if (!userCreated.equals(other.userCreated))
      return false;
    if (virtualResourceGroup == null) {
      if (other.virtualResourceGroup != null)
        return false;
    }
    else if (!virtualResourceGroup.equals(other.virtualResourceGroup))
      return false;
    return true;
  }

}
