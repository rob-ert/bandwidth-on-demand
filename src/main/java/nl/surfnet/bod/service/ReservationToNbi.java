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

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.repo.ReservationRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReservationToNbi {

  @Autowired
  private NbiClient nbiClient;
  @Autowired
  private ReservationRepo reservationRepo;
  @Autowired
  private ReservationEventPublisher reservationEventPublisher;

  @Async
  public void submitNewReservation(Long reservationId, boolean autoProvision) {
    final Reservation reservation = reservationRepo.findOne(reservationId);
    final ReservationStatus orgStatus = reservation.getStatus();

    Reservation reservationWithReservationId = nbiClient.createReservation(reservation, autoProvision);

    reservationRepo.save(reservationWithReservationId);

    publishStatusChanged(reservationWithReservationId, orgStatus);
  }

  private void publishStatusChanged(final Reservation reservation, final ReservationStatus originalStatus) {
    if (originalStatus == reservation.getStatus()) {
      return;
    }

    ReservationStatusChangeEvent createEvent = new ReservationStatusChangeEvent(originalStatus, reservation);
    reservationEventPublisher.notifyListeners(createEvent);
  }
}