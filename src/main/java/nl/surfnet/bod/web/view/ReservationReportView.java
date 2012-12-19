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
package nl.surfnet.bod.web.view;

import org.joda.time.DateTime;

public class ReservationReportView {

  private final DateTime periodStart;
  private final DateTime periodEnd;

  // Reservation requests
  private long amountRequestsCreatedSucceeded;
  private long amountRequestsCreatedFailed;
  private long amountRequestsModifiedSucceeded;
  private long amountRequestsModifiedFailed;
  private long amountRequestsCancelSucceeded;
  private long amountRequestsCancelFailed;

  // By channel
  private long amountRequestsThroughGUI;
  private long amountRequestsThroughNSI;

  // Reservations per protectionType
  private long amountReservationsProtected;
  private long amountReservationsUnprotected;
  private long amountReservationsRedundant;

  // Running reservations
  private long amountRunningReservationsSucceeded;
  private long amountRunningReservationsFailed;
  private long amountRunningReservationsStillRunning;
  private long amountRunningReservationsNeverProvisioned;
  private long amountRunningReservationsStillScheduled;

  public ReservationReportView(DateTime start, DateTime end) {
    this.periodStart = start;
    this.periodEnd = end;
  }

  public long getTotalRequests() {
    return amountRequestsCreatedSucceeded + amountRequestsCreatedFailed + amountRequestsModifiedSucceeded
        + amountRequestsModifiedFailed + amountRequestsCancelSucceeded + amountRequestsCancelFailed;
  }

  public long getTotalReservations() {
    return amountReservationsProtected + amountReservationsUnprotected + amountReservationsRedundant;

  }

  public long getTotalActiveReservations() {
    return amountRunningReservationsSucceeded + amountRunningReservationsFailed + amountRunningReservationsStillRunning
        + amountRunningReservationsStillScheduled + amountRunningReservationsNeverProvisioned;
  }

  public long getTotalAmountRequestsCreated() {
    return amountRequestsCreatedSucceeded + amountRequestsCreatedFailed;
  }

  public long getTotalAmountRequestsModified() {
    return amountRequestsModifiedSucceeded + amountRequestsModifiedFailed;
  }

  public long getTotalAmountRequestsCancelled() {
    return amountRequestsCancelSucceeded + amountRequestsCancelFailed;
  }

  public long getAmountRequestsCreatedSucceeded() {
    return amountRequestsCreatedSucceeded;
  }

  public void setAmountRequestsCreatedSucceeded(long amountRequestsCreatedSucceeded) {
    this.amountRequestsCreatedSucceeded = amountRequestsCreatedSucceeded;
  }

  public long getAmountRequestsCreatedFailed() {
    return amountRequestsCreatedFailed;
  }

  public void setAmountRequestsCreatedFailed(long amountRequestsCreatedFailed) {
    this.amountRequestsCreatedFailed = amountRequestsCreatedFailed;
  }

  public long getAmountRequestsModifiedSucceeded() {
    return amountRequestsModifiedSucceeded;
  }

  public void setAmountRequestsModifiedSucceeded(long amountRequestsModifiedSucceeded) {
    this.amountRequestsModifiedSucceeded = amountRequestsModifiedSucceeded;
  }

  public long getAmountRequestsModifiedFailed() {
    return amountRequestsModifiedFailed;
  }

  public void setAmountRequestsModifiedFailed(long amountRequestsModifiedFailed) {
    this.amountRequestsModifiedFailed = amountRequestsModifiedFailed;
  }

  public long getAmountRequestsCancelSucceeded() {
    return amountRequestsCancelSucceeded;
  }

  public void setAmountRequestsCancelSucceeded(long amountRequestsCancelSucceeded) {
    this.amountRequestsCancelSucceeded = amountRequestsCancelSucceeded;
  }

  public long getAmountRequestsCancelFailed() {
    return amountRequestsCancelFailed;
  }

  public void setAmountRequestsCancelFailed(long amountRequestsCancelFailed) {
    this.amountRequestsCancelFailed = amountRequestsCancelFailed;
  }

  public long getAmountRequestsThroughGUI() {
    return amountRequestsThroughGUI;
  }

  public void setAmountRequestsThroughGUI(long amountRequestsThroughGUI) {
    this.amountRequestsThroughGUI = amountRequestsThroughGUI;
  }

  public long getAmountRequestsThroughNSI() {
    return amountRequestsThroughNSI;
  }

  public void setAmountRequestsThroughNSI(long amountRequestsThroughNSI) {
    this.amountRequestsThroughNSI = amountRequestsThroughNSI;
  }

  public long getAmountReservationsProtected() {
    return amountReservationsProtected;
  }

  public void setAmountReservationsProtected(long amountReservationsProtected) {
    this.amountReservationsProtected = amountReservationsProtected;
  }

  public long getAmountReservationsUnprotected() {
    return amountReservationsUnprotected;
  }

  public void setAmountReservationsUnprotected(long amountReservationsUnprotected) {
    this.amountReservationsUnprotected = amountReservationsUnprotected;
  }

  public long getAmountReservationsRedundant() {
    return amountReservationsRedundant;
  }

  public void setAmountReservationsRedundant(long amountReservationsRedundant) {
    this.amountReservationsRedundant = amountReservationsRedundant;
  }

  public long getAmountRunningReservationsSucceeded() {
    return amountRunningReservationsSucceeded;
  }

  public void setAmountRunningReservationsSucceeded(long amountRunningReservationsSucceeded) {
    this.amountRunningReservationsSucceeded = amountRunningReservationsSucceeded;
  }

  public long getAmountRunningReservationsFailed() {
    return amountRunningReservationsFailed;
  }

  public void setAmountRunningReservationsFailed(long amountRunningReservationsFailed) {
    this.amountRunningReservationsFailed = amountRunningReservationsFailed;
  }

  public long getAmountRunningReservationsStillRunning() {
    return amountRunningReservationsStillRunning;
  }

  public void setAmountRunningReservationsStillRunning(long amountRunningReservationsStillRunning) {
    this.amountRunningReservationsStillRunning = amountRunningReservationsStillRunning;
  }

  public void setAmounRunningReservationsStillScheduled(long amountRunningReservationsStillScheduled) {
    this.amountRunningReservationsStillScheduled = amountRunningReservationsStillScheduled;
  }

  public long getAmountRunningReservationsStillScheduled() {
    return amountRunningReservationsStillScheduled;
  }

  public long getAmountRunningReservationsNeverProvisioned() {
    return amountRunningReservationsNeverProvisioned;
  }

  public void setAmountRunningReservationsNeverProvisioned(long amountRunningReservationsNeverProvisioned) {
    this.amountRunningReservationsNeverProvisioned = amountRunningReservationsNeverProvisioned;
  }

  public DateTime getPeriodStart() {
    return periodStart;
  }

  public DateTime getPeriodEnd() {
    return periodEnd;
  }

}
