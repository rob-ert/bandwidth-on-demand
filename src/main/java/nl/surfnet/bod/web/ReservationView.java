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
package nl.surfnet.bod.web;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;

import org.joda.time.LocalDateTime;

public class ReservationView {
  private final Long id;
  private final String virtualResourceGroup;
  private final String sourcePort;
  private final String destinationPort;
  private final ReservationStatus status;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final Integer bandwidth;
  private final String userCreated;
  private final String reservationId;

  public ReservationView(Reservation reservation) {
    this.id = reservation.getId();
    this.virtualResourceGroup = reservation.getVirtualResourceGroup().getName();
    this.sourcePort = reservation.getSourcePort().getUserLabel();
    this.destinationPort = reservation.getDestinationPort().getUserLabel();
    this.status = reservation.getStatus();
    this.startDateTime = reservation.getStartDateTime();
    this.endDateTime = reservation.getEndDateTime();
    this.bandwidth = reservation.getBandwidth();
    this.userCreated = reservation.getUserCreated();
    this.reservationId = reservation.getReservationId();
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

}