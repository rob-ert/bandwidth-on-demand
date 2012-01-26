package nl.surfnet.bod.web.push;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;

public final class Events {

  private Events() {

  }

  public static Event createSimpleEvent(String groupId, String message) {
    return new SimpleEvent(groupId, message);
  }

  public static Event createReservationStatusChangedEvent(Reservation reservation, ReservationStatus oldStatus) {

    String message = String.format("The status of Reservation {} , was changed from {} to {}", new Object[] {
        reservation.getReservationId(), oldStatus, reservation.getStatus() });

    return new SimpleEvent(reservation.getVirtualResourceGroup().getName(), message);
  }

  public static final class SimpleEvent implements Event {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private String groupId;
    private String message;

    public SimpleEvent(String groupId, String message) {
      this.groupId = groupId;
      this.message = message;

      log.debug("Created event for groupId {} with message {}", groupId, message);
    }

    @Override
    public String getGroupId() {
      return groupId;
    }

    @Override
    public String getMessage() {
      return message;
    }

  }
}
