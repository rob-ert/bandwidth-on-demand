package nl.surfnet.bod.service;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;

public class ReservationStatusChangeEvent {

  private ReservationStatus oldStatus;

  private Reservation reservation;

  public ReservationStatusChangeEvent(final ReservationStatus oldStatus, final Reservation reservation) {
    this.oldStatus = oldStatus;
    this.reservation = reservation;
  }

  public ReservationStatus getOldStatus() {
    return oldStatus;
  }

  public Reservation getReservation() {
    return reservation;
  }

}
