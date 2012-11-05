package nl.surfnet.bod.web.view;

import org.joda.time.DateTime;

public class NocReservationReport {

  private final DateTime periodStart;
  private final DateTime periodEnd;

  private long amountFailedReservationRequests;
  private long amountSucceededReservationRequests;

  private long amountProtectedReservations;
  private long amountUnprotectedReservations;
  private long amountRedundantReservations;

  private long amountNSIReservations;
  private long amountGUIReservations;

  private long amountSucceededReservations;
  private long amountCancelledReservations;
  private long amountFailedReservations;

  public NocReservationReport(DateTime start, DateTime end) {
    this.periodStart = start;
    this.periodEnd = end;
  }

  public DateTime getPeriodStart() {
    return periodStart;
  }

  public DateTime getPeriodEnd() {
    return periodEnd;
  }

  // Calculated amounts
  public long getTotalReservationRequests() {
    return amountFailedReservationRequests + amountSucceededReservationRequests;
  }

  public long getTotalReservations() {
    return amountSucceededReservationRequests;
  }

  public long getAmountFailedReservationRequests() {
    return amountFailedReservationRequests;
  }

  public void setAmountFailedReservationRequests(long amountFailedReservationRequests) {
    this.amountFailedReservationRequests = amountFailedReservationRequests;
  }

  public long getAmountSucceededReservationRequests() {
    return amountSucceededReservationRequests;
  }

  public void setAmountSucceededReservationRequests(long amountSucceededReservationRequests) {
    this.amountSucceededReservationRequests = amountSucceededReservationRequests;
  }

  public long getAmountProtectedReservations() {
    return amountProtectedReservations;
  }

  public void setAmountProtectedReservations(long amountProtectedReservations) {
    this.amountProtectedReservations = amountProtectedReservations;
  }

  public long getAmountUnprotectedReservations() {
    return amountUnprotectedReservations;
  }

  public void setAmountUnprotectedReservations(long amountUnprotectedReservations) {
    this.amountUnprotectedReservations = amountUnprotectedReservations;
  }

  public long getAmountRedundantReservations() {
    return amountRedundantReservations;
  }

  public void setAmountRedundantReservations(long amountRedundantReservations) {
    this.amountRedundantReservations = amountRedundantReservations;
  }

  public long getAmountNSIReservations() {
    return amountNSIReservations;
  }

  public void setAmountNSIReservations(long amountNSIReservations) {
    this.amountNSIReservations = amountNSIReservations;
  }

  public long getAmountGUIReservations() {
    return amountGUIReservations;
  }

  public void setAmountGUIReservations(long amountGUIReservations) {
    this.amountGUIReservations = amountGUIReservations;
  }

  public long getAmountSucceededReservations() {
    return amountSucceededReservations;
  }

  public void setAmountSucceededReservations(long amountSucceededReservations) {
    this.amountSucceededReservations = amountSucceededReservations;
  }

  public long getAmountCancelledReservations() {
    return amountCancelledReservations;
  }

  public void setAmountCancelledReservations(long amountCancelledReservations) {
    this.amountCancelledReservations = amountCancelledReservations;
  }

  public long getAmountFailedReservations() {
    return amountFailedReservations;
  }

  public void setAmountFailedReservations(long amountFailedReservations) {
    this.amountFailedReservations = amountFailedReservations;
  }

}
