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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import nl.surfnet.bod.domain.PhysicalPort_;
import nl.surfnet.bod.domain.PhysicalResourceGroup_;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.Reservation_;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualPort_;
import nl.surfnet.bod.domain.VirtualResourceGroup_;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.repo.ReservationRepo;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.ReadablePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
  private NbiClient nbiClient;

  @Autowired
  private ReservationEventPublisher reservationEventPublisher;

  @Autowired
  private EntityManagerFactory entityManagerFactory;

  private ExecutorService executorService = Executors.newCachedThreadPool();

  /**
   * Reserves a reservation using the {@link NbiClient} asynchronously.
   * 
   * @param reservation
   * @return
   */
  public Future<?> create(Reservation reservation) {
    checkState(reservation.getSourcePort().getVirtualResourceGroup().equals(reservation.getVirtualResourceGroup()));
    checkState(reservation.getDestinationPort().getVirtualResourceGroup().equals(reservation.getVirtualResourceGroup()));

    // Make sure reservations occur on whole minutes only
    reservation.setStartTime(reservation.getStartTime().withSecondOfMinute(0).withMillisOfSecond(0));
    reservation.setEndTime(reservation.getEndTime().withSecondOfMinute(0).withMillisOfSecond(0));

    reservationRepo.save(reservation);

    return executorService.submit(new ReservationSubmitter(reservation));
  }

  public Reservation find(Long id) {
    return reservationRepo.findOne(id);
  }

  public List<Reservation> findEntries(int firstResult, int maxResults, Sort sort) {
    final RichUserDetails user = Security.getUserDetails();

    if (user.getUserGroups().isEmpty()) {
      return Collections.emptyList();
    }

    return reservationRepo.findAll(forCurrentUser(user), new PageRequest(firstResult / maxResults, maxResults, sort))
        .getContent();
  }

  public Collection<Reservation> findByVirtualPort(VirtualPort port) {
    return reservationRepo.findBySourcePortOrDestinationPort(port, port);
  }

  public long count() {
    final RichUserDetails user = Security.getUserDetails();

    if (user.getUserGroups().isEmpty()) {
      return 0;
    }

    return reservationRepo.count(forCurrentUser(user));
  }

  public Reservation update(Reservation reservation) {
    checkState(reservation.getSourcePort().getVirtualResourceGroup().equals(reservation.getVirtualResourceGroup()));
    checkState(reservation.getDestinationPort().getVirtualResourceGroup().equals(reservation.getVirtualResourceGroup()));

    log.debug("Updating reservation: {}", reservation.getReservationId());
    return reservationRepo.save(reservation);
  }

  public boolean cancel(Reservation reservation) {
    if (reservation.getStatus() == RUNNING || reservation.getStatus() == SCHEDULED) {
      reservation.setStatus(CANCELLED);
      nbiClient.cancelReservation(reservation.getReservationId());
      reservationRepo.save(reservation);

      return true;
    }
    return false;
  }

  public ReservationStatus getStatus(Reservation reservation) {
    return nbiClient.getReservationStatus(reservation.getReservationId());
  }

  /**
   * Finds all reservations which start or ends on the given dateTime and have a
   * status which can still change its status.
   * 
   * @param dateTime
   *          {@link LocalDateTime} to search for
   * @return list of found Reservations
   */
  public List<Reservation> findReservationsToPoll(LocalDateTime dateTime) {
    return reservationRepo.findAll(specReservationsToPoll(dateTime));
  }

  public List<Reservation> findEntriesForManager(final RichUserDetails manager, int firstResult, int maxResults,
      Sort sort) {
    if (manager.getUserGroups().isEmpty()) {
      return Collections.emptyList();
    }

    return reservationRepo.findAll(forManager(manager), new PageRequest(firstResult / maxResults, maxResults, sort))
        .getContent();
  }

  public long countForManager(final RichUserDetails manager) {
    return reservationRepo.count(forManager(manager));
  }

  private Specification<Reservation> forManager(final RichUserDetails manager) {
    return new Specification<Reservation>() {
      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.and(cb.or(
            root.get(Reservation_.sourcePort).get(VirtualPort_.physicalPort).get(PhysicalPort_.physicalResourceGroup)
                .get(PhysicalResourceGroup_.adminGroup).in(manager.getUserGroupIds()),
            root.get(Reservation_.destinationPort).get(VirtualPort_.physicalPort)
                .get(PhysicalPort_.physicalResourceGroup).get(PhysicalResourceGroup_.adminGroup)
                .in(manager.getUserGroupIds())));
      }
    };
  }

  private Specification<Reservation> forCurrentUser(final RichUserDetails user) {
    return new Specification<Reservation>() {
      @Override
      public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.and(root.get(Reservation_.virtualResourceGroup).get(VirtualResourceGroup_.surfconextGroupId)
            .in(user.getUserGroupIds()));
      }
    };
  }

  private Specification<Reservation> specReservationsToPoll(final LocalDateTime startOrEndDateTime) {
    return new Specification<Reservation>() {
      @Override
      public javax.persistence.criteria.Predicate toPredicate(Root<Reservation> reservation, CriteriaQuery<?> query,
          CriteriaBuilder cb) {

        return cb.and(
            cb.or(
                cb.and(cb.equal(reservation.get(Reservation_.startTime), startOrEndDateTime.toLocalTime()),
                    cb.equal(reservation.get(Reservation_.startDate), startOrEndDateTime.toLocalDate())),
                cb.and(cb.equal(reservation.get(Reservation_.endTime), startOrEndDateTime.toLocalTime()),
                    cb.equal(reservation.get(Reservation_.endDate), startOrEndDateTime.toLocalDate()))),
            cb.and(reservation.get(Reservation_.status).in(ReservationStatus.TRANSITION_STATES)));
      }
    };
  }

  public List<Reservation> findReservationsInYear(Integer year) {

    LocalDate start = new DateMidnight().withYear(year).withMonthOfYear(DateTimeConstants.JANUARY).withDayOfMonth(01)
        .toLocalDate();

    LocalDate end = new DateMidnight().withYear(year).withMonthOfYear(DateTimeConstants.DECEMBER).withDayOfMonth(31)
        .toLocalDate();

    return reservationRepo.findByStartDateBetweenOrEndDateBetween(start, end, start, end);
  }

  public List<Reservation> findReservationsForCommingPeriod(ReadablePeriod period) {

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime end = now.plus(period);

    return reservationRepo.findByStartDateBetweenOrEndDateBetween(now.toLocalDate(), end.toLocalDate(),
        now.toLocalDate(), end.toLocalDate());
  }

  public List<Reservation> findReservationsForElapsedPeriod(ReadablePeriod period) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime past = now.minus(period);

    return reservationRepo.findByStartDateBetweenOrEndDateBetween(past.toLocalDate(), now.toLocalDate(),
        past.toLocalDate(), now.toLocalDate());
  }

  public List<Double> findUniqueYearsFromReservations() {

    RichUserDetails userDetails = Security.getUserDetails();
    // FIXME add userDetails to query

    final String queryString = "select distinct extract(year from start_date)  startYear from reservation UNION select distinct extract(year from end_date) from reservation";

    List<Double> resultList = entityManagerFactory.createEntityManager().createNativeQuery(queryString).getResultList();

    Collections.sort(resultList);

    return resultList;
  }

  /**
   * Asynchronous {@link Reservation} creator.
   * 
   */
  private final class ReservationSubmitter implements Runnable {
    private final Reservation reservation;
    private final ReservationStatus originalStatus;

    public ReservationSubmitter(Reservation reservation) {
      this.reservation = reservation;
      this.originalStatus = reservation.getStatus();
    }

    @Override
    public void run() {
      Reservation createdReservation = nbiClient.createReservation(reservation);

      update(createdReservation);

      publishStatusChanged(createdReservation);
    }

    private void publishStatusChanged(Reservation newReservation) {
      ReservationStatusChangeEvent createEvent = new ReservationStatusChangeEvent(originalStatus, newReservation);

      reservationEventPublisher.notifyListeners(createEvent);
    }
  }

}
