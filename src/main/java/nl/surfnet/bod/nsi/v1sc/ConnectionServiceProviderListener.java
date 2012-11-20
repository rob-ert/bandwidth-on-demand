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
package nl.surfnet.bod.nsi.v1sc;

import static nl.surfnet.bod.web.WebUtils.not;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.service.ReservationEventPublisher;
import nl.surfnet.bod.service.ReservationListener;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.ReservationStatusChangeEvent;

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
  private ConnectionServiceRequesterCallback connectionServiceRequester;

  @Resource
  private ReservationService reservationService;

  @PostConstruct
  public void registerListener() {
    reservationEventPublisher.addListener(this);
  }

  @Override
  public void onStatusChange(ReservationStatusChangeEvent event) {
    try {
      logger.debug("Got a reservation status change event {}", event);

      Reservation reservation = reservationService.find(event.getReservation().getId());

      if (not(reservation.isNSICreated())) {
        logger.debug("Reservation {} was not created using NSI, no work to perform", reservation.getLabel());
        return;
      }

      Connection connection = reservation.getConnection().get();

      switch (event.getNewStatus()) {
      case RESERVED:
        connectionServiceRequester.reserveConfirmed(connection, event.getNsiRequestDetails().get());
        break;
      case AUTO_START:
        connectionServiceRequester.provisionSucceeded(connection);
        break;
      case FAILED:
        handleReservationFailed(connection, event);
        break;
      case NOT_ACCEPTED:
        Optional<String> failedReason = Optional.fromNullable(Strings.emptyToNull(event.getReservation()
          .getFailedReason()));
        connectionServiceRequester.reserveFailed(connection, event.getNsiRequestDetails().get(), failedReason);
        break;
      case TIMED_OUT:
        connectionServiceRequester.terminateTimedOutReservation(connection);
        break;
      case RUNNING:
        connectionServiceRequester.provisionConfirmed(connection, event.getNsiRequestDetails().get());
        break;
      case CANCELLED:
        connectionServiceRequester.terminateConfirmed(connection, event.getNsiRequestDetails());
        break;
      case CANCEL_FAILED:
        connectionServiceRequester.terminateFailed(connection, event.getNsiRequestDetails());
        break;
      default:
        logger.error("Unhandled status {} of reservation {}", event.getNewStatus(), event.getReservation());
      }
    }
    catch (Exception e) {
      logger.error("Handeling status change failed " + event, e);
    }
  }

  private void handleReservationFailed(Connection connection, ReservationStatusChangeEvent event) {
    // FIXME AvD add RUNNING
    if (connection.getCurrentState() == ConnectionStateType.AUTO_PROVISION
        || connection.getCurrentState() == ConnectionStateType.SCHEDULED) {
      // the connection is was ready to get started but the step to running/provisioned failed
      // so send a provisionFailed
      connectionServiceRequester.provisionFailed(connection, event.getNsiRequestDetails().get());
    }
    else if (connection.getCurrentState() == ConnectionStateType.TERMINATING) {
      connectionServiceRequester.terminateFailed(connection, event.getNsiRequestDetails());
    }
    else {
      logger.error("Listener got a failed '{}' but did not know what to do with the connection {}", event.getNewStatus(), connection);
    }
  }

}
