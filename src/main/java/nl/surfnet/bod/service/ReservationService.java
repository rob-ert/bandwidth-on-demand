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
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.repo.ReservationRepo;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
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
        log.debug("About to update reservation [{}] status changed from [{}] to: {} ",
            new String[] { reservation.getReservationId(), reservation.getStatus().name(), actualStatus.name() });

        reservation.setStatus(actualStatus);
        update(reservation);
        updatedItems++;
      }
    }
    log.info("Amount of reservations checked [{}] from which [{}] needed a status update", reservations.size(),
        updatedItems);
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

    monitorReservationStatus(ReservationStatus.SCHEDULED, reservation);
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

  /**
   * Starts monitoring all reservation which have an status in
   * {@link ReservationStatus#TRANSITION_STATES} and which start or end in the
   * future.
   * 
   * If the reservation starts in the future, the monitor is started on the
   * start time. If the reservation ends in the future the monitor starts at the
   * end time.
   */
  @Scheduled(cron = "0 0 " + "${reservation.poll.future.starthour}" + " * * *")
  public void checkFutureReservationsForStatusChange() {
    int scheduledMonitors = 0;
    LocalDateTime now = new LocalDateTime();

    log.info("Checking future reservations for status changes");

    List<Reservation> reservations = reservationRepo.findAll(specFutureReservationsForStatusChange(now));

    for (Reservation reservation : reservations) {
      if (now.isBefore(reservation.getStartDateTime())) {
        scheduledMonitors++;

        log.debug("Scheduled reservation [{}] with state [{}] is based on startDateTime: {}", new String[] {
            reservation.getReservationId(), reservation.getStatus().name(), reservation.getStartDateTime().toString() });

        monitorReservationStatus(reservation.getStartDateTime().toDate(), reservation);
      }
      else if (now.isBefore(reservation.getEndDateTime())) {
        scheduledMonitors++;

        log.debug("Scheduled reservation [{}] with state [{}] is based on endDateTime: {}",
            new String[] { reservation.getReservationId(), reservation.getStatus().name(),
                reservation.getEndDateTime().toString() });

        monitorReservationStatus(reservation.getEndDateTime().toDate(), reservation);
      }
      else {
        log.warn("Reservation [{}]  is not monitored, but it should be...", reservation);
      }
    }

    log.info("Amount of future reservations checked [{}], amount of monitored reservations: {}", reservations.size(),
        scheduledMonitors);
  }

   Specification<Reservation> specFutureReservationsForStatusChange(final LocalDateTime now) {
    return new Specification<Reservation>() {
      @Override
      public javax.persistence.criteria.Predicate toPredicate(Root<Reservation> reservation, CriteriaQuery<?> query,
          CriteriaBuilder cb) {
        Expression<LocalDate> startDateExpr = reservation.get("startDate");
        Expression<LocalTime> startTimeExpr = reservation.get("startTime");
        Expression<LocalDate> endDateExpr = reservation.get("endDate");
        Expression<LocalTime> endTimeExpr = reservation.get("endTime");
        Expression<ReservationStatus> status = reservation.get("status");

        Predicate predicate = cb.and(cb.greaterThanOrEqualTo(startTimeExpr, now.toLocalTime()),
            (cb.greaterThanOrEqualTo(startDateExpr, now.toLocalDate())));
        cb.or(cb.and(cb.greaterThanOrEqualTo(endTimeExpr, now.toLocalDate()),
            cb.greaterThanOrEqualTo(endDateExpr, now.toLocalDate())));
        cb.and(status.in(ReservationStatus.TRANSITION_STATES));

        return predicate;
      }
    };
  }

  public ReservationStatus getStatus(Reservation reservation) {
    return nbiService.getReservationStatus(reservation.getReservationId());
  }

  /**
   * Starts the {@link ReservationPoller#monitorStatus(Reservation)}, updates
   * the given {@link Reservation} when a status change occurs.
   * 
   * @param reservations
   *          The {@link Reservation}s to monitor
   */
  public void monitorReservationStatus(Reservation... reservations) {
    reservationPoller.monitorStatus(null, reservations);
  }

  /**
   * Starts the {@link ReservationPoller#monitorStatus(Reservation)}, at the
   * given time. Updates the given {@link Reservation} when a status change
   * occurs.
   * 
   * @param start
   *          specifies when to start monitoring
   * 
   * @param reservations
   *          The {@link Reservation}s to monitor
   * 
   */
  public void monitorReservationStatus(Date start, Reservation reservation) {
    reservationPoller.monitorStatusWIthSpecificStart(start, reservation);
  }

  /**
   * Starts the {@link ReservationPoller#monitorStatus(Reservation)}, updates
   * the given {@link Reservation} when a status change occurs.
   * 
   * Schedules monitoring for expected status changes by calling
   * {@link #checkAllReservationsForStatusUpdate()}
   * 
   * @param stopStatus
   *          whenever this {@link ReservationStatus} is reached to monitoring
   *          will stop.
   * 
   * @param reservations
   *          The {@link Reservation}s to monitor
   */
  public void monitorReservationStatus(ReservationStatus stopStatus, Reservation... reservations) {
    reservationPoller.monitorStatus(stopStatus, reservations);

    checkFutureReservationsForStatusChange();
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
