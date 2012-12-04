/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
    return Objects.toStringHelper(this).add("reservationId", reservation.getId()).add("oldStatus", oldStatus).add(
        "newStatus", getNewStatus()).add("nsiRequest", nsiRequestDetails).toString();
  }

}
