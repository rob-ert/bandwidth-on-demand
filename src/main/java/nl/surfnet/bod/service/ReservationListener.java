package nl.surfnet.bod.service;

public interface ReservationListener {
  
  void onStatusChange(ReservationStatusChangeEvent reservationStatusChangeEvent);

}
