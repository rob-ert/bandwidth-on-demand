package nl.surfnet.bod.nsi.v2;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.domain.NsiVersion;
import nl.surfnet.bod.service.ReservationEventPublisher;
import nl.surfnet.bod.service.ReservationListener;
import nl.surfnet.bod.service.ReservationStatusChangeEvent;

import com.google.common.base.Optional;

@Component
public class ConnectionServiceProviderListenerV2 implements ReservationListener {

  @Resource private ReservationEventPublisher reservationEventPublisher;
  @Resource private ConnectionServiceRequesterV2Callback requester;

  @PostConstruct
  public void registerListener() {
    reservationEventPublisher.addListener(this);
  }

  @Override
  public void onStatusChange(ReservationStatusChangeEvent event) {

    Optional<Connection> connection = event.getReservation().getConnection();

    System.err.println("Change v2 detected.." + connection);

    if (!connection.isPresent() || connection.get().getNsiVersion() != NsiVersion.TWO) {
      return;
    }

    ConnectionV2 connectionV2 = (ConnectionV2) connection.get();

    switch (event.getNewStatus()) {
    case RESERVED:
      requester.reserveConfirmed(connectionV2, event.getNsiRequestDetails().get());
      break;
    default:
      throw new RuntimeException("ARGG not implemented..");
    }
  }

}
