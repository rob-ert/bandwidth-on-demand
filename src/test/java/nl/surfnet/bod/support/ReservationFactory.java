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
  private ReservationStatus status = ReservationStatus.AUTO_START;
  private VirtualPort sourcePort;
  private VirtualPort destinationPort;
  private DateTime startDateTime = DateTime.now().withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0);
  private DateTime endDateTime = startDateTime.plusDays(1).plus(WebUtils.DEFAULT_RESERVATON_DURATION);
  private String userCreated = "urn:truusvisscher";
  private Integer bandwidth = 10000;
  private String reservationId = "9" + String.valueOf(id);
  private String failedReason;
  private String cancelReason;
  private final VirtualResourceGroup virtualResourceGroup = new VirtualResourceGroupFactory().create();
  private Connection connection;
  private ProtectionType protectionType = ProtectionType.PROTECTED;

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
    reservation.setFailedReason(failedReason);
    reservation.setCancelReason(cancelReason);
    reservation.setConnection(connection);
    reservation.setProtectionType(protectionType);

    return reservation;
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

  public ReservationFactory setConnection(Connection connection) {
    this.connection = connection;
    return this;
  }

  public ReservationFactory setStartAndDuration(DateTime start, ReadablePeriod period) {
    setStartDateTime(start);

    setEndDateTime(start.plus(period));

    return this;
  }

  public ReservationFactory withNodId() {
    this.id = null;
    this.version = null;

    return this;
  }

}