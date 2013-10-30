/**
 * Copyright (c) 2012, 2013 SURFnet BV
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
package nl.surfnet.bod.domain;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public final class UpdatedReservationStatus {
  private final ReservationStatus newStatus;
  private final Optional<String> cancelReason;
  private final Optional<String> failedReason;

  private UpdatedReservationStatus(ReservationStatus newStatus, Optional<String> cancelReason, Optional<String> failedReason) {
    this.newStatus = newStatus;
    this.cancelReason = cancelReason;
    this.failedReason = failedReason;
  }

  public static UpdatedReservationStatus forNewStatus(ReservationStatus newStatus) {
    return new UpdatedReservationStatus(newStatus, Optional.<String>absent(), Optional.<String>absent());
  }

  public static UpdatedReservationStatus cancelling(String cancelReason) {
    return new UpdatedReservationStatus(ReservationStatus.CANCELLING, Optional.of(cancelReason), Optional.<String>absent());
  }

  public static UpdatedReservationStatus cancelFailed(String failedReason) {
    return error(ReservationStatus.CANCEL_FAILED, failedReason);
  }

  public static UpdatedReservationStatus failed(String failedReason) {
    return error(ReservationStatus.FAILED, failedReason);
  }

  public static UpdatedReservationStatus notAccepted(String failedReason) {
    return error(ReservationStatus.NOT_ACCEPTED, failedReason);
  }

  public static UpdatedReservationStatus error(ReservationStatus status, String failedReason) {
    Preconditions.checkArgument(status.isErrorState());
    return new UpdatedReservationStatus(status, Optional.<String>absent(), Optional.of(failedReason));
  }

  public ReservationStatus getNewStatus() {
    return newStatus;
  }

  public Optional<String> getCancelReason() {
    return cancelReason;
  }

  public Optional<String> getFailedReason() {
    return failedReason;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(newStatus).append(cancelReason).append(failedReason).toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    UpdatedReservationStatus that = (UpdatedReservationStatus) obj;
    return new EqualsBuilder()
        .append(this.newStatus, that.newStatus)
        .append(this.cancelReason, that.cancelReason)
        .append(this.failedReason, that.failedReason)
        .isEquals();
  }

  @Override
  public String toString() {
    return "UpdatedReservationStatus [newStatus=" + newStatus + ", cancelReason=" + cancelReason + ", failedReason=" + failedReason + "]";
  }

}
