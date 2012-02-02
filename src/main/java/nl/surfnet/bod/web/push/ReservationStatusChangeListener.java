package nl.surfnet.bod.web.push;

import javax.annotation.PostConstruct;

import nl.surfnet.bod.service.ReservationEventPublisher;
import nl.surfnet.bod.service.ReservationListener;
import nl.surfnet.bod.service.ReservationStatusChangeEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReservationStatusChangeListener implements ReservationListener {

  @Autowired
  private ReservationEventPublisher reservationEventPublisher;

  @Autowired
  private EndPoints endPoints;

  @PostConstruct
  public void registerListener() {
    reservationEventPublisher.addListener(this);
  }

  @Override
  public void onStatusChange(ReservationStatusChangeEvent reservationStatusChangeEvent) {
    Event event = Events.createEvent(reservationStatusChangeEvent);

    endPoints.broadcast(event);
  }

}
