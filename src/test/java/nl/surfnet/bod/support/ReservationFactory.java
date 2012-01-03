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
package nl.surfnet.bod.support;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

public class ReservationFactory {

  private Long id;
  private Integer version;
  private VirtualResourceGroup vRGroup = new VirtualResourceGroupFactory().create();
  private ReservationStatus reservationStatus = ReservationStatus.PENDING;
  private VirtualPort sourcePort = new VirtualPortFactory().create();
  private VirtualPort destinationPort = new VirtualPortFactory().create();
  private LocalDate startDate = LocalDate.now();
  private LocalDate endDate = LocalDate.now().plusDays(1);
  private LocalTime startTime = new LocalTime(12, 0);
  private LocalTime endTime = new LocalTime(16, 0);
  private String user = "urn:truusvisscher";

  public Reservation create() {

    Reservation reservation = new Reservation();
    reservation.setId(id);
    reservation.setVersion(version);
    reservation.setStatus(reservationStatus);
    reservation.setSourcePort(sourcePort);
    reservation.setDestinationPort(destinationPort);
    reservation.setVirtualResourceGroup(vRGroup);
    reservation.setStartDate(startDate);
    reservation.setStartTime(startTime);
    reservation.setEndDate(endDate);
    reservation.setEndTime(endTime);
    reservation.setUser(user);

    return reservation;
  }

  public ReservationFactory setId(Long id) {
    this.id = id;
    return this;
  }

  public ReservationFactory setVersion(Integer version) {
    this.version = version;
    return this;
  }

  public ReservationFactory setVirtualResourceGroup(VirtualResourceGroup vRGroup) {
    this.vRGroup = vRGroup;
    return this;
  }

  public ReservationFactory setReservationStatus(ReservationStatus status) {
    this.reservationStatus = status;
    return this;
  }

  public ReservationFactory setSourcePort(VirtualPort sourcePort) {
    this.sourcePort = sourcePort;
    return this;
  }

  public ReservationFactory setDestinationPort(VirtualPort endPort) {
    this.destinationPort = endPort;
    return this;
  }

  public ReservationFactory setUser(String user) {
    this.user = user;
    return this;
  }

  public ReservationFactory setEndDate(LocalDate endDate) {
    this.endDate = endDate;
    return this;
  }

  public ReservationFactory setStartDate(LocalDate startDate) {
    this.startDate = startDate;
    return this;
  }

  public ReservationFactory setStartTime(LocalTime startTime) {
    this.startTime = startTime;
    return this;
  }

  public ReservationFactory setEndTime(LocalTime endTime) {
    this.endTime = endTime;
    return this;
  }
}
