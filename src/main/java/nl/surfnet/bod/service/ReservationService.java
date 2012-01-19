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

import static com.google.common.base.Preconditions.checkState;
import static nl.surfnet.bod.domain.ReservationStatus.CANCELLED;
import static nl.surfnet.bod.domain.ReservationStatus.RUNNING;
import static nl.surfnet.bod.domain.ReservationStatus.SCHEDULED;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.repo.ReservationRepo;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReservationService {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private ReservationRepo reservationRepo;

  @Autowired
  @Qualifier("nbiService")
  private NbiService nbiService;

  @Autowired
  private ReservationPoller reservationPoller;

  @PostConstruct
  public void checkAllReservationsForStatusUpdate() {
    long updatedItems = 0;

    List<Reservation> reservations = reservationRepo.findByStatusIn(ReservationStatus.TRANSITION_STATES);

    for (Reservation reservation : reservations) {
      ReservationStatus actualStatus = getStatus(reservation);
      if (reservation.getStatus() != actualStatus) {
        log.debug("About to update reservation [" + reservation.getReservationId() + "] status changed from ["
            + reservation.getStatus() + "] to: " + actualStatus);

        reservation.setStatus(actualStatus);
        update(reservation);
        updatedItems++;
      }
    }
    log.info("Amount of reservations checked [" + reservations.size() + "] from which [" + updatedItems
        + "] needed a status update");
  }

  public void reserve(Reservation reservation) throws ReservationFailedException {
    checkState(reservation.getSourcePort().getVirtualResourceGroup().equals(reservation.getVirtualResourceGroup()));
    checkState(reservation.getDestinationPort().getVirtualResourceGroup().equals(reservation.getVirtualResourceGroup()));

    final String reservationId = nbiService.createReservation(reservation);
    if (reservationId == null) {
      throw new ReservationFailedException("Unable to create reservation: " + reservation);
    }
    reservation.setReservationId(reservationId);
    reservation.setStatus(getStatus(reservation));
    reservationRepo.save(reservation);

    monitorReservationStatus(reservation);
  }

  public Reservation find(Long id) {
    return reservationRepo.findOne(id);
  }

  public Collection<Reservation> findEntries(int firstResult, int maxResults) {
    final RichUserDetails user = Security.getUserDetails();

    if (user.getUserGroups().isEmpty()) {
      return Collections.emptyList();
    }

    return reservationRepo.findAll(forCurrentUser(user), new PageRequest(firstResult / maxResults, maxResults))
        .getContent();
  }

  public long count() {
    final RichUserDetails user = Security.getUserDetails();

    if (user.getUserGroups().isEmpty()) {
      return 0;
    }

    return reservationRepo.count(forCurrentUser(user));
  }

  private Specification<Reservation> forCurrentUser(final RichUserDetails user) {
    return new Specification<Reservation>() {
      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.and(root.get("virtualResourceGroup").get("surfConextGroupName").in(user.getUserGroupIds()));
      }
    };
  }

  public Reservation update(Reservation reservation) {
    checkState(reservation.getSourcePort().getVirtualResourceGroup().equals(reservation.getVirtualResourceGroup()));
    checkState(reservation.getDestinationPort().getVirtualResourceGroup().equals(reservation.getVirtualResourceGroup()));

    return reservationRepo.save(reservation);
  }

  public void cancel(Reservation reservation) {
    if (reservation.getStatus() == RUNNING || reservation.getStatus() == SCHEDULED) {
      reservation.setStatus(CANCELLED);
      nbiService.cancelReservation(reservation.getReservationId());
      reservationRepo.save(reservation);

      monitorReservationStatus(reservation);
    }
  }

  public ReservationStatus getStatus(Reservation reservation) {
    return nbiService.getReservationStatus(reservation.getReservationId());
  }

  /**
   * Starts the {@link ReservationPoller#monitorStatus(Reservation)}, updates
   * the given {@link Reservation} when a status change occurs.
   * 
   * @param reservation
   *          The {@link Reservation} to monitor
   */
  public void monitorReservationStatus(Reservation reservation) {
    reservationPoller.monitorStatus(reservation);
  }

  /**
   * 
   * @param reservationStatus
   *          {@link ReservationStatus} to evaluate
   * @return true when the reservationStatus is an endState, false otherwise
   * @see ReservationStatus
   * 
   */
  public boolean isEndState(ReservationStatus reservationStatus) {
    return reservationStatus != null && reservationStatus.isEndState(reservationStatus);
  }

  /**
   * 
   * @param reservationStatus
   *          {@link ReservationStatus} to evaluate
   * @return true when the reservationStatus is a transitionState, false
   *         otherwise
   * @see ReservationStatus
   * 
   */
  public boolean isTransitionState(ReservationStatus reservationStatus) {
    return !isEndState(reservationStatus);
  }
}
