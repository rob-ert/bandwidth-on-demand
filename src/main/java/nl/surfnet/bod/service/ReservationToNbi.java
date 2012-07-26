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
package nl.surfnet.bod.service;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;

import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.repo.ReservationRepo;
import nl.surfnet.bod.web.security.Security;

@Service
public class ReservationToNbi {

  private Logger logger = LoggerFactory.getLogger(ReservationToNbi.class);

  @Resource
  private NbiClient nbiClient;

  @Resource
  private ReservationRepo reservationRepo;

  @Resource
  private ReservationEventPublisher reservationEventPublisher;

  @Resource
  private LogEventService logEventService;

  @Async
  public void reserve(Reservation reservation, boolean autoProvision, Optional<NsiRequestDetails> requestDetails) {
    logger.debug("Requesting a new reservation from the Nbi, {} ({})", reservation);

    ReservationStatus orgStatus = reservation.getStatus();
    reservation = nbiClient.createReservation(reservation, autoProvision);

    reservation = reservationRepo.save(reservation);
    publishStatusChanged(reservation, orgStatus, requestDetails);
  }

  @Async
  public void provision(Reservation reservation, Optional<NsiRequestDetails> requestDetails) {
    logger.debug("Activating a reservation {}", reservation);

    boolean activateReservation = nbiClient.activateReservation(reservation.getReservationId());

    if (activateReservation) {
      ReservationStatus orgStatus = reservation.getStatus();

      reservation.setStatus(ReservationStatus.SCHEDULED);
      reservationRepo.save(reservation);

      publishStatusChanged(reservation, orgStatus, requestDetails);
    }
  }

  private void publishStatusChanged(
      Reservation reservation, ReservationStatus originalStatus, Optional<NsiRequestDetails> nsiRequestDetails) {
    if (originalStatus == reservation.getStatus()) {
      logger.debug("No status change detected from {} to {}", originalStatus, reservation.getStatus());
      return;
    }

    ReservationStatusChangeEvent createEvent = new ReservationStatusChangeEvent(originalStatus, reservation, nsiRequestDetails);

    logEventService.logUpdateEvent(Security.getUserDetails(), reservation, "State change: " + createEvent);

    reservationEventPublisher.notifyListeners(createEvent);
  }

}