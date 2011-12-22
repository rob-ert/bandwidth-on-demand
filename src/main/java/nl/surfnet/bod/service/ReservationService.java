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

import java.util.Collection;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.repo.ReservationRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReservationService {

  @Autowired
  private ReservationRepo reservationRepo;

  public void save(Reservation reservation) {
    reservationRepo.save(reservation);
  }

  public Reservation find(Long id) {
    return reservationRepo.findOne(id);
  }

  public Collection<Reservation> findEntries(int firstResult, int maxResults) {
    return reservationRepo.findAll(new PageRequest(firstResult / maxResults, maxResults)).getContent();
  }

  public long count() {
    return reservationRepo.count();
  }

  public Reservation update(Reservation reservation) {
    return reservationRepo.save(reservation);
  }

  public void delete(Reservation reservation) {
    reservationRepo.delete(reservation);
  }

  public ReservationStatus makeReservation(Reservation reservation) {
    ReservationStatus reservationStatus = ReservationStatus.PENDING;

    //Check

    return reservationStatus;
  }

}
