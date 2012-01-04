package nl.surfnet.bod.service;

@SuppressWarnings("serial")
public class ReservationFailedException extends RuntimeException {

  public ReservationFailedException(String message, Throwable cause) {
    super(message, cause);
  }

  public ReservationFailedException(String message) {
    super(message);
  }

}
