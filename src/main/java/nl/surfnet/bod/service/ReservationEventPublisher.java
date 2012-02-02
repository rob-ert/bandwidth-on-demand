package nl.surfnet.bod.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class ReservationEventPublisher {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private List<ReservationListener> listeners = Lists.newArrayList();

  public void addListener(ReservationListener reservationListener) {
    this.listeners.add(reservationListener);
  }
  
  public void notifyListeners(ReservationStatusChangeEvent changeEvent) {
    for (ReservationListener listener : listeners) {
      log.info("notify listeners {}", changeEvent);
      listener.onStatusChange(changeEvent);
    }
  }

}
