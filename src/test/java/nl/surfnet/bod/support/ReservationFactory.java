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

import java.util.concurrent.atomic.AtomicLong;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.web.user.ReservationController;

import org.joda.time.LocalDateTime;
import org.joda.time.ReadablePeriod;

import com.google.common.base.Strings;

public class ReservationFactory {

  private static final AtomicLong COUNTER = new AtomicLong();

  private static Long id = COUNTER.incrementAndGet();
  private Integer version;
  private String name;
  private ReservationStatus status = ReservationStatus.SCHEDULED;
  private VirtualPort sourcePort;
  private VirtualPort destinationPort;
  private LocalDateTime startDateTime = LocalDateTime.now().withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0);
  private LocalDateTime endDateTime = startDateTime.plusDays(1).plus(ReservationController.DEFAULT_RESERVATON_DURATION);
  private String userCreated = "urn:truusvisscher";
  private Integer bandwidth = 10000;
  private String reservationId = "9" + String.valueOf(id);
  private String failedMessage;
  private VirtualResourceGroup virtualResourceGroup = new VirtualResourceGroupFactory().setSurfconextGroupId(SURF_CONEXT_GROUP_ID).create();

  public final static String SURF_CONEXT_GROUP_ID = "urn:the:same";
  
  public Reservation create() {
    sourcePort = sourcePort == null ? new VirtualPortFactory().setVirtualResourceGroup(virtualResourceGroup).create()
        : sourcePort;
    destinationPort = destinationPort == null ? new VirtualPortFactory().setVirtualResourceGroup(virtualResourceGroup)
        .create() : destinationPort;

    Reservation reservation = new Reservation();
    reservation.setId(id);
    reservation.setVersion(version);
    reservation.setName(name);
    reservation.setStatus(status);
    reservation.setSourcePort(sourcePort);
    reservation.setDestinationPort(destinationPort);
    reservation.setStartDateTime(startDateTime);
    reservation.setEndDateTime(endDateTime);
    reservation.setUserCreated(userCreated);
    reservation.setBandwidth(bandwidth);
    reservation.setReservationId(reservationId);
    reservation.setFailedMessage(failedMessage);

    return reservation;
  }

  public ReservationFactory setName(String name) {
    this.name = name;
    return this;
  }

  public ReservationFactory setId(Long id) {
    this.id = id;
    return this;
  }

  public ReservationFactory setFailedMessage(String failedMessage) {
    this.failedMessage = failedMessage;
    return this;
  }

  public ReservationFactory setVersion(Integer version) {
    this.version = version;
    return this;
  }

  public ReservationFactory setStatus(ReservationStatus status) {
    this.status = status;
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

  public ReservationFactory setUserCreated(String user) {
    this.userCreated = user;
    return this;
  }

  public ReservationFactory setEndDateTime(LocalDateTime endDateTime) {
    this.endDateTime = endDateTime;
    return this;
  }

  public ReservationFactory setStartDateTime(LocalDateTime startDateTime) {
    this.startDateTime = startDateTime;
    return this;
  }

  public ReservationFactory setBandwidth(Integer bandwidth) {
    this.bandwidth = bandwidth;
    return this;
  }

  public ReservationFactory setReservationId(String reservationid) {
    this.reservationId = reservationid;
    return this;
  }

  public ReservationFactory setStartAndDuration(LocalDateTime start, ReadablePeriod period) {
    setStartDateTime(start);

    setEndDateTime(start.plus(period));

    return this;
  }

}
