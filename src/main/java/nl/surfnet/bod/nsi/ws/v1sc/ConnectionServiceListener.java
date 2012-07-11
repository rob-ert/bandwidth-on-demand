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