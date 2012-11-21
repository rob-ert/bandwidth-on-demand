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

import org.joda.time.DateTime;

public class NocReservationReport {

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
  private long amounRunningReservationsStillRunning;
  private long amountRunningReservationsNeverProvisioned;

  public NocReservationReport(DateTime start, DateTime end) {
    this.periodStart = start;
    this.periodEnd = end;
  }

  public long getTotalRequests() {
    return amountRequestsCreatedSucceeded + amountRequestsCreatedFailed + amountRequestsModifiedSucceeded
        + amountRequestsModifiedFailed + amountRequestsCancelSucceeded + amountRequestsCancelFailed;
  }

  public long getTotalReservations() {
    return amountReservationsProtected + amountReservationsUnprotected + amountReservationsRedundant
        + amountRequestsCreatedSucceeded;
  }

  public long getTotalActiveReservations() {
    return amountRunningReservationsSucceeded + amountRunningReservationsFailed + amounRunningReservationsStillRunning
        + amountRunningReservationsNeverProvisioned;
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

  public long getAmounRunningReservationsStillRunning() {
    return amounRunningReservationsStillRunning;
  }

  public void setAmounRunningReservationsStillRunning(long amounRunningReservationsStillRunning) {
    this.amounRunningReservationsStillRunning = amounRunningReservationsStillRunning;
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
