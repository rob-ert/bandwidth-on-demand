package nl.surfnet.bod.nsi.ws.v1sc;

import javax.annotation.PostConstruct;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.nsi.ws.NsiProvider;
import nl.surfnet.bod.repo.ReservationRepo;
import nl.surfnet.bod.service.ReservationEventPublisher;
import nl.surfnet.bod.service.ReservationListener;
import nl.surfnet.bod.service.ReservationStatusChangeEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConnectionServiceListener implements ReservationListener {

  private final Logger logger = LoggerFactory.getLogger(ConnectionServiceListener.class);

  @Autowired
  private ReservationEventPublisher reservationEventPublisher;

  @Autowired
  private NsiProvider nsiProvider;

  @Autowired
  private ReservationRepo reservationRepo;

  @PostConstruct
  public void registerListener() {
    reservationEventPublisher.addListener(this);
  }

  @Override
  public void onStatusChange(ReservationStatusChangeEvent event) {
    logger.debug("Got a reservation status change event {}", event);
    Reservation reservation = event.getReservation();

    switch (reservation.getStatus()) {
    case SCHEDULED:
      nsiProvider.reserveConfirmed(reservation.getConnection());
      break;
    case FAILED:
      nsiProvider.reserveFailed(reservation.getConnection());
      break;
    default:
      logger.error("Unhandled status {} of reservation {}", reservation.getStatus(), event.getReservation());
    }

  }

}