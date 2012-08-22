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
package nl.surfnet.bod.service;

import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

public class ReservationStatusChangeEvent {

  private final ReservationStatus oldStatus;
  private final Reservation reservation;
  private final Optional<NsiRequestDetails> nsiRequestDetails;


  public ReservationStatusChangeEvent(ReservationStatus oldStatus, Reservation reservation) {
    this(oldStatus, reservation, Optional.<NsiRequestDetails>absent());
  }

  public ReservationStatusChangeEvent(ReservationStatus oldStatus, Reservation reservation,
      Optional<NsiRequestDetails> nsiRequestDetails) {
    this.oldStatus = oldStatus;
    this.reservation = reservation;
    this.nsiRequestDetails = nsiRequestDetails;
  }

  public ReservationStatus getOldStatus() {
    return oldStatus;
  }

  public ReservationStatus getNewStatus() {
    return reservation.getStatus();
  }

  public Reservation getReservation() {
    return reservation;
  }

  public Optional<NsiRequestDetails> getNsiRequestDetails() {
    return nsiRequestDetails;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("reservationId", reservation.getId())
        .add("oldStatus", oldStatus)
        .add("newStatus", getNewStatus())
        .add("nsiRequest", nsiRequestDetails).toString();
  }

}
