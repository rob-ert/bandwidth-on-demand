/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.support;

import static nl.surfnet.bod.domain.ReservationStatus.AUTO_START;

import java.util.concurrent.atomic.AtomicLong;

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.web.WebUtils;

import org.joda.time.DateTime;
import org.joda.time.ReadablePeriod;

public class ReservationFactory {

  private static final AtomicLong COUNTER = new AtomicLong();

  private Long id = COUNTER.incrementAndGet();
  private Integer version;
  private String name = "Default name";
  private ReservationStatus status = AUTO_START;
  private VirtualPort sourcePort;
  private VirtualPort destinationPort;
  private DateTime startDateTime = DateTime.now().withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
  private DateTime endDateTime = startDateTime.plusDays(1).plus(WebUtils.DEFAULT_RESERVATON_DURATION);
  private String userCreated = "urn:truusvisscher";
  private Integer bandwidth = 10000;
  private String reservationId = "9" + String.valueOf(id);
  private String failedReason;
  private String cancelReason;
  private VirtualResourceGroup virtualResourceGroup;
  private ConnectionV1 connectionV1;
  private ConnectionV2 connectionV2;
  private ProtectionType protectionType = ProtectionType.PROTECTED;

  private boolean noIds = false;

  public Reservation create() {
    sourcePort = sourcePort == null ? createVirtualPort() : sourcePort;
    destinationPort = destinationPort == null ? createVirtualPort() : destinationPort;

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
    reservation.setFailedReason(failedReason);
    reservation.setCancelReason(cancelReason);
    if (connectionV1 != null) {
      reservation.setConnectionV1(connectionV1);
    } else if (connectionV2 != null) {
      reservation.setConnectionV2(connectionV2);
    }
    reservation.setProtectionType(protectionType);

    if (connectionV1 != null) {
      connectionV1.setReservation(reservation);
    }
    if (connectionV2 != null) {
      connectionV2.setReservation(reservation);
    }

    return reservation;
  }

  private VirtualResourceGroup createVirtualResourceGroup() {
    if (virtualResourceGroup != null) {
      return virtualResourceGroup;
    }

    VirtualResourceGroupFactory factory = new VirtualResourceGroupFactory();

    if (noIds) {
      factory.withNoIds();
    }

    this.virtualResourceGroup = factory.create();

    return virtualResourceGroup;
  }

  private VirtualPort createVirtualPort() {
    VirtualPortFactory factory = new VirtualPortFactory().setVirtualResourceGroup(createVirtualResourceGroup());

    if (noIds) {
      factory.withNodIds();
    }

    return factory.create();
  }

  public ReservationFactory withProtection() {
    protectionType = ProtectionType.PROTECTED;
    return this;
  }

  public ReservationFactory withoutProtection() {
    protectionType = ProtectionType.UNPROTECTED;
    return this;
  }

  public ReservationFactory setName(String name) {
    this.name = name;
    return this;
  }

  public ReservationFactory setId(Long id) {
    this.id = id;
    return this;
  }

  public ReservationFactory setFailedReason(String failedReason) {
    this.failedReason = failedReason;
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

  public ReservationFactory setEndDateTime(DateTime endDateTime) {
    this.endDateTime = endDateTime;
    return this;
  }

  public ReservationFactory setStartDateTime(DateTime startDateTime) {
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

  public ReservationFactory setCancelReason(String cancelReason) {
    this.cancelReason = cancelReason;
    return this;
  }

  public ReservationFactory setConnectionV1(ConnectionV1 connection) {
    this.connectionV1 = connection;
    return this;
  }

  public ReservationFactory setConnectionV2(ConnectionV2 connection) {
    this.connectionV2 = connection;
    return this;
  }

  public ReservationFactory setStartAndDuration(DateTime start, ReadablePeriod period) {
    setStartDateTime(start);

    setEndDateTime(start.plus(period));

    return this;
  }

  public ReservationFactory withNoIds() {
    this.id = null;
    this.version = null;
    this.noIds = true;

    return this;
  }

}