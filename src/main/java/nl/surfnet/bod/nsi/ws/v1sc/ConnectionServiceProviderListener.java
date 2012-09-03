/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.nsi.ws.v1sc;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.nsi.ws.ConnectionServiceProvider;
import nl.surfnet.bod.service.ReservationEventPublisher;
import nl.surfnet.bod.service.ReservationListener;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.ReservationStatusChangeEvent;
import static nl.surfnet.bod.web.WebUtils.not;

import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

@Component
public class ConnectionServiceProviderListener implements ReservationListener {

  private final Logger logger = LoggerFactory.getLogger(ConnectionServiceProviderListener.class);

  @Resource
  private ReservationEventPublisher reservationEventPublisher;

  @Resource
  private ConnectionServiceProvider connectionServiceProvider;

  @Resource
  private ReservationService reservationService;

  @PostConstruct
  public void registerListener() {
    reservationEventPublisher.addListener(this);
  }

  @Override
  public void onStatusChange(ReservationStatusChangeEvent event) {
    logger.debug("Got a reservation status change event {}", event);

    Reservation reservation = reservationService.find(event.getReservation().getId());

    if (not(reservation.isNSICreated())) {
      logger.debug("Reservation {} was not created using NSI, no work to perform", reservation.getLabel());
      return;
    }

    Connection connection = reservation.getConnection();

    switch (event.getNewStatus()) {
    case RESERVED:
      connectionServiceProvider.reserveConfirmed(connection, event.getNsiRequestDetails().get());
      break;
    case SCHEDULED:
      // no need to send back any confirms, a provision confirm is only sent
      // when start time has passed in which case
      // the status would be RUNNING
      break;
    case FAILED:
      handleReservationFailed(connection, event);
      break;
    case RUNNING:
      connectionServiceProvider.provisionConfirmed(connection, event.getNsiRequestDetails().get());
      break;
    case CANCELLED:
      connectionServiceProvider.terminateConfirmed(connection, event.getNsiRequestDetails().get());
      break;
    default:
      logger.error("Unhandled status {} of reservation {}", event.getNewStatus(), event.getReservation());
    }
  }

  private void handleReservationFailed(Connection connection, ReservationStatusChangeEvent event) {

    try {
      logger.debug("Connection state {}, new reservation state {}", connection.getCurrentState(), event.getNewStatus());

      if (connection.getCurrentState() == ConnectionStateType.AUTO_PROVISION
          || connection.getCurrentState() == ConnectionStateType.SCHEDULED) {
        connectionServiceProvider.provisionFailed(connection, event.getNsiRequestDetails().get());
      }
      else if (connection.getCurrentState() == ConnectionStateType.TERMINATING) {
        connectionServiceProvider.terminateFailed(connection, event.getNsiRequestDetails().get());
      }
      else if (connection.getCurrentState() == ConnectionStateType.RESERVING) {
        Optional<String> failedReason = Optional.fromNullable(Strings.emptyToNull(event.getReservation()
            .getFailedReason()));
        connectionServiceProvider.reserveFailed(connection, event.getNsiRequestDetails().get(), failedReason);
      }
    }
    catch (Exception e) {
      logger.warn("Handeling failed, failed", e);
    }
  }

}
