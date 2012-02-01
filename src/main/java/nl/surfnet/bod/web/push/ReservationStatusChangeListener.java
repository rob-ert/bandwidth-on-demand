package nl.surfnet.bod.web.push;

import javax.annotation.PostConstruct;

import nl.surfnet.bod.service.ReservationListener;
import nl.surfnet.bod.service.ReservationPoller;
import nl.surfnet.bod.service.ReservationStatusChangeEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReservationStatusChangeListener implements ReservationListener {
  
  @Autowired
  private ReservationPoller reservationPoller;
  
  @Autowired
  private EndPoints endPoints;
  
  @PostConstruct
  public void registerListener() {
    reservationPoller.addListener(this);
  }
  
  @Override
  public void onStatusChange(ReservationStatusChangeEvent reservationStatusChangeEvent) {
    Event event = Events.createReservationStatusChangedEvent(reservationStatusChangeEvent);
    
    endPoints.broadcast(event);  
  }

}
