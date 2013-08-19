/**
 * Copyright (c) 2012, 2013 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static nl.surfnet.bod.domain.ReservationStatus.RUNNING;
import static nl.surfnet.bod.service.ReservationPredicatesAndSpecifications.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.repo.ReservationArchiveRepo;
import nl.surfnet.bod.repo.ReservationRepo;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.ElementActionView;
import nl.surfnet.bod.web.view.ReservationFilterView;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

@Service
@Transactional
public class ReservationService extends AbstractFullTextSearchService<Reservation> {

  @VisibleForTesting
  static final ObjectMapper mapper = new ObjectMapper();
  static {
    mapper.setVisibilityChecker(
      mapper.getSerializationConfig().getDefaultVisibilityChecker()
        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
        .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
  }

  private static final Function<Reservation, ReservationArchive> toReservationArchive = new Function<Reservation, ReservationArchive>() {
    @Override
    public ReservationArchive apply(Reservation reservation) {
      ReservationArchive reservationArchive = new ReservationArchive();
      reservationArchive.setReservationPrimaryKey(reservation.getId());

      StringWriter writer = new StringWriter();
      try {
        mapper.writeValue(writer, reservation);
      } catch (IOException e) {
        // need to throw or we'll lose reservations
        throw new RuntimeException(e);
      }
      reservationArchive.setReservationAsJson(writer.toString());

      return reservationArchive;
    }
  };

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Resource private ReservationRepo reservationRepo;
  @Resource private ReservationArchiveRepo reservationArchiveRepo;
  @Resource private NbiClient nbiClient;
  @Resource private ReservationToNbi reservationToNbi;
  @Resource private LogEventService logEventService;

  @PersistenceContext private EntityManager entityManager;

  void alignConnectionWithReservation(Reservation reservation) {
    if (reservation.isNSICreated()) {
      Connection connection = reservation.getConnection().get();
      connection.setStartTime(reservation.getStartDateTime());
      connection.setEndTime(reservation.getEndDateTime());
    }
  }

  public Optional<Future<Long>> cancel(Reservation reservation, RichUserDetails user) {
    return cancelWithReason(reservation, "Cancelled by " + user.getDisplayName(), user);
  }

  public void cancelAndArchiveReservations(List<Reservation> reservations, RichUserDetails user) {
    for (Reservation reservation : reservations) {
      if (isDeleteAllowedForUserOnly(reservation, user).isAllowed() && reservation.getStatus().isTransitionState()) {

        if (StringUtils.hasText(reservation.getReservationId())) {
          ReservationStatus reservationState = nbiClient.cancelReservation(reservation.getReservationId());
          reservation.setStatus(reservationState);

          if (reservationState == ReservationStatus.CANCEL_FAILED) {
            throw new RuntimeException("NMS Error during cancel of reservation: " + reservation.getReservationId());
          }
        }
      }
    }

    Collection<ReservationArchive> reservationArchives = transformToReservationArchives(reservations);
    reservationArchiveRepo.save(reservationArchives);
    logEventService.logCreateEvent(Security.getUserDetails(), reservationArchives);

    reservationRepo.delete(reservations);
    logEventService.logDeleteEvent(Security.getUserDetails(), "Canceled, archived and deleted", reservations);
  }

  public Optional<Future<Long>> cancelWithReason(Reservation reservation, String cancelReason, RichUserDetails user) {

    if (isDeleteAllowed(reservation, user).isAllowed() && reservation.getStatus().isTransitionState()) {
      return Optional.of(reservationToNbi.asyncTerminate(reservation.getId(), cancelReason));
    }

    log.info("Not allowed to cancel reservation {} with state {}", reservation.getName(), reservation.getStatus());

    return Optional.absent();
  }

  public void cancelDueToReserveTimeout(final Long reservationId) {
    reservationToNbi.asyncTerminate(reservationId, "Canceled due to reserve held timeout");
  }

  private void correctStart(Reservation reservation) {
    if (reservation.getStartDateTime() == null || reservation.getStartDateTime().isBeforeNow()) {
      reservation.setStartDateTime(DateTime.now().plusMinutes(1));
    }
  }

  public long countActiveReservationsByVirtualPorts(List<VirtualPort> virtualPorts) {
    if (CollectionUtils.isEmpty(virtualPorts)) {
      return 0;
    }

    Specification<Reservation> whereClause = ReservationPredicatesAndSpecifications.specActiveByVirtualPorts(virtualPorts);

    return reservationRepo.count(whereClause);
  }

  public long countActiveReservationsForGroup(VirtualResourceGroup group) {
    return countForFilterAndVirtualResourceGroup(ReservationFilterViewFactory.ACTIVE, group);
  }

  public long countActiveReservationsForPhysicalPort(UniPort port) {
    return reservationRepo.count(Specifications.where(specByPhysicalPort(port)).and(specActiveReservations()));
  }

  public long countAllEntriesUsingFilter(ReservationFilterView filter) {
    return reservationRepo.count(specFilteredReservations(filter));
  }

  public long countComingReservationsForGroup(VirtualResourceGroup group) {
    return countForFilterAndVirtualResourceGroup(ReservationFilterViewFactory.COMING, group);
  }

  public long countElapsedReservationsForGroup(VirtualResourceGroup group) {
    return countForFilterAndVirtualResourceGroup(ReservationFilterViewFactory.ELAPSED, group);
  }

  public long countForFilterAndManager(RichUserDetails manager, ReservationFilterView filter) {
    return reservationRepo.count(specFilteredReservationsForManager(filter, manager));
  }

  public long countForFilterAndUser(RichUserDetails user, ReservationFilterView filter) {
    if (user.getUserGroupIds().isEmpty()) {
      return 0;
    }

    return reservationRepo.count(specFilteredReservationsForUser(filter, user));
  }

  public long countForFilterAndVirtualResourceGroup(String filterId, VirtualResourceGroup vrg) {
    if (vrg == null) {
      return 0;
    }

    ReservationFilterView filterView = new ReservationFilterViewFactory().create(filterId);
    return reservationRepo.count(specFilteredReservationsForVirtualResourceGroup(filterView, vrg));
  }

  public long countForPhysicalPort(UniPort port) {
    return reservationRepo.count(specByPhysicalPort(port));
  }

  public long countForUser(RichUserDetails user) {
    if (user.getUserGroups().isEmpty()) {
      return 0;
    }

    return reservationRepo.count(forCurrentUser(user));
  }

  public long countForVirtualResourceGroup(VirtualResourceGroup vrg) {
    return reservationRepo.count(forVirtualResourceGroup(vrg));
  }

  /**
   * Creates a {@link Reservation} which is auto provisioned
   *
   * @param reservation
   * @See {@link #create(Reservation)}
   */
  public Future<Long> create(Reservation reservation) {
    return create(reservation, true);
  }

  /**
   * Reserves a reservation using the {@link NbiClient} asynchronously.
   *
   * @param reservation
   * @param autoProvision
   *          , indicates if the reservations should be automatically
   *          provisioned
   * @return ReservationId, scheduleId from NMS
   *
   */
  public Future<Long> create(Reservation reservation, boolean autoProvision) {
    checkState(reservation.hasConsistentVirtualResourceGroups(), "virtual resource groups of reservation do not match");

    correctStart(reservation);
    stripSecondsAndMillis(reservation);
    alignConnectionWithReservation(reservation);

    reservation = reservationRepo.save(reservation);
    logEventService.logCreateEvent(Security.getUserDetails(), reservation);

    return reservationToNbi.asyncReserve(reservation.getId(), autoProvision);
  }

  public Reservation find(Long id) {
    return reservationRepo.findOne(id);
  }

  public Collection<Reservation> findActiveByPhysicalPort(UniPort port) {
    return reservationRepo.findAll(Specifications.where(specByPhysicalPort(port)).and(specActiveReservations()));
  }

  public Collection<Reservation> findAllActiveByVirtualPort(VirtualPort port) {
    return reservationRepo.findAll(Specifications.where(specByVirtualPort(port)).and(specActiveReservations()));
  }

  public Collection<Reservation> findAllActiveByVirtualPortForManager(VirtualPort port, RichUserDetails user) {
    return reservationRepo.findAll(Specifications.where(specByVirtualPort(port)).and(specActiveReservations()).and(
        specByVirtualPortAndManager(port, user)));
  }

  public List<Reservation> findAllEntriesUsingFilter(ReservationFilterView filter, int firstResult,
      int maxResults, Sort sort) {

    return reservationRepo.findAll(specFilteredReservations(filter),
        new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
  }

  public Reservation findByConnectionId(String connectionId) {
    return reservationRepo.findByConnectionV1ConnectionId(connectionId);
  }

  public Reservation findByReservationId(String reservationId) {
    return reservationRepo.findByReservationId(reservationId);
  }

  public List<Reservation> findBySourcePortOrDestinationPort(VirtualPort virtualPortA, VirtualPort virtualPortB) {
    return reservationRepo.findBySourcePortVirtualPortOrDestinationPortVirtualPort(virtualPortA, virtualPortB);
  }

  public Collection<Reservation> findByVirtualPort(VirtualPort port) {
    return reservationRepo.findBySourcePortVirtualPortOrDestinationPortVirtualPort(port, port);
  }

  public List<Reservation> findEntries(int firstResult, int maxResults, Sort sort) {
    RichUserDetails user = Security.getUserDetails();

    if (user.getUserGroups().isEmpty()) {
      return Collections.emptyList();
    }

    return reservationRepo.findAll(forCurrentUser(user), new PageRequest(firstResult / maxResults, maxResults, sort))
        .getContent();
  }

  public List<Reservation> findEntriesForManagerUsingFilter(RichUserDetails manager, ReservationFilterView filter,
      int firstResult, int maxResults, Sort sort) {

    return reservationRepo.findAll(specFilteredReservationsForManager(filter, manager),
        new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
  }

  public List<Reservation> findEntriesForUserUsingFilter(RichUserDetails user,
      ReservationFilterView filter, int firstResult, int maxResults, Sort sort) {

    if (user.getUserGroupIds().isEmpty()) {
      return Collections.emptyList();
    }

    List<Reservation> content = reservationRepo.findAll(specFilteredReservationsForUser(filter, user),
        new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();

    return content;
  }

  public List<Long> findIdsForManagerUsingFilter(RichUserDetails manager, ReservationFilterView filter, Sort sort) {
    return reservationRepo.findIdsWithWhereClause(specFilteredReservationsForManager(filter, manager), Optional.fromNullable(sort));
  }

  public List<Long> findIdsForNocUsingFilter(ReservationFilterView filter, Sort sort) {
    return reservationRepo.findIdsWithWhereClause(specFilteredReservations(filter), Optional.fromNullable(sort));
  }

  public List<Long> findIdsForUserUsingFilter(RichUserDetails user, ReservationFilterView filter, Sort sort) {
    return reservationRepo.findIdsWithWhereClause(specFilteredReservationsForUser(filter, user), Optional.fromNullable(sort));
  }

  /**
   * Finds all reservations which start or ends on the given dateTime and have a
   * status which can still change its status.
   *
   * @param dateTime
   *          {@link org.joda.time.LocalDateTime} to search for
   * @return list of found Reservations
   */
  public Collection<Reservation> findReservationsToPoll(DateTime dateTime) {
    Set<Reservation> reservations = Sets.newHashSet();
    reservations.addAll(reservationRepo.findAll(specReservationsThatCouldStart(dateTime)));
    reservations.addAll(reservationRepo.findAll(specReservationsThatAreTimedOutAndTransitionally(dateTime)));
    reservations.addAll(findReservationWithStatus(RUNNING));

    return reservations;
  }

  public Collection<Reservation> findTransitionableReservations() {
    return reservationRepo.findAll(ReservationPredicatesAndSpecifications.specActiveReservations());
  }

  private List<Reservation> findReservationWithStatus(ReservationStatus... states) {
    return reservationRepo.findByStatusIn(Arrays.asList(states));
  }

  public List<Integer> findUniqueYearsFromReservations() {
    // FIXME Franky add UserDetails to query
    @SuppressWarnings("unchecked")
    List<Double> dbYears = entityManager.createNativeQuery(
        "select distinct extract(year from start_date_time) startYear "
            + "from reservation UNION select distinct extract(year from end_date_time) from reservation")
        .getResultList();

    ImmutableList<Integer> years = FluentIterable.from(dbYears).filter(Predicates.notNull()).transform(
        new Function<Double, Integer>() {
          @Override
          public Integer apply(Double d) {
            return d.intValue();
          }
        }).toList();

    return Ordering.natural().sortedCopy(years);
  }

  @Override
  protected EntityManager getEntityManager() {
    return entityManager;
  }

  /**
   * A reservation is allowed to be delete for the following cases:
   * <ul>
   * <li>a manager may delete a reservation to minimal one of his ports</li>
   * <li>or</li>
   * <li>a user may delete a reservation if he is a member of the
   * virtualResourceGroup of the reservation</li>
   * <li>and</li>
   * <li>the current status of the reservation must allow it</li>
   * </ul>
   *
   * @param reservation
   *          {@link Reservation} to check
   * @param role
   *          {@link BodRole} the selected user role
   * @return true if the reservation is allowed to be delete, false otherwise
   */
  public ElementActionView isDeleteAllowed(Reservation reservation, RichUserDetails user) {
    if (!reservation.getStatus().isDeleteAllowed()) {
      return new ElementActionView(false, "reservation_state_transition_not_allowed");
    }

    return isDeleteAllowedForUserOnly(reservation, user);
  }

  private ElementActionView isDeleteAllowedForUserOnly(Reservation reservation, RichUserDetails user) {
    BodRole role = user.getSelectedRole();

    if (role.isNocRole()) {
      return new ElementActionView(true, "label_cancel");
    } else if (role.isManagerRole()
        && (isReservationDeleteAllowedForPortAsManager(role, reservation.getSourcePort())
            || isReservationDeleteAllowedForPortAsManager(role, reservation.getDestinationPort()))) {
      return new ElementActionView(true, "label_cancel");
    } else if (role.isUserRole() && user.isMemberOf(reservation.getVirtualResourceGroup().getAdminGroup())) {
      return new ElementActionView(true, "label_cancel");
    } else {
      return new ElementActionView(false, "reservation_cancel_user_has_no_rights");
    }
  }

  private boolean isReservationDeleteAllowedForPortAsManager(BodRole managerRole, ReservationEndPoint sourcePort) {
    Preconditions.checkArgument(managerRole.isManagerRole(), "must be manager");
    return sourcePort.getUniPort().isPresent()
        && sourcePort.getUniPort().get().getPhysicalResourceGroup().getId().equals(managerRole.getPhysicalResourceGroupId().get());
  }

  public ElementActionView isEditAllowed(Reservation reservation, BodRole role) {
    if (role.isNocRole()) {
      return new ElementActionView(false, "reservation_edit_user_has_no_rights");
    } else if (role.isManagerRole()) {
      return new ElementActionView(false, "reservation_copy_user_has_no_rights");
    } else if (role.isUserRole() && Security.isUserMemberOf(reservation.getVirtualResourceGroup())) {
      return new ElementActionView(true, "label_copy");
    }

    return new ElementActionView(false, "reservation_edit_user_has_no_rights");
  }

  /**
   * Activates an existing reservation;
   *
   * @param reservation {@link Reservation} to activate
   */
  public void provision(Reservation reservation) {
    checkNotNull(reservation);

    reservationToNbi.asyncProvision(reservation.getId());
  }

  private void stripSecondsAndMillis(Reservation reservation) {
    if (reservation.getStartDateTime() != null) {
      reservation.setStartDateTime(reservation.getStartDateTime().withSecondOfMinute(0).withMillisOfSecond(0));
    }
    if (reservation.getEndDateTime() != null) {
      reservation.setEndDateTime(reservation.getEndDateTime().withSecondOfMinute(0).withMillisOfSecond(0));
    }
  }

  @VisibleForTesting
  Collection<ReservationArchive> transformToReservationArchives(List<Reservation> reservations) {
    return Collections2.transform(reservations, toReservationArchive);
  }

  /**
   * Will update the status of the specified reservation if it is different than its current
   * @param reservationId
   * @param newStatus
   * @throws javax.persistence.NoResultException when no Reservation with the specified reservationId exists
   * @return the reservation containing the new status
   */
  public Reservation updateStatus(String reservationId, ReservationStatus newStatus) {
    // Avoid optimistic locking exceptions by pessimistically locking a reservation, so only a single
    // thread can update the row at the same time.
    Reservation reservation = reservationRepo.getByReservationIdWithPessimisticWriteLock(reservationId);
    ReservationStatus oldStatus = reservation.getStatus();
    if (oldStatus == newStatus) {
      log.debug("Reservation ({}) status unchanged at {}", newStatus);
      return reservation;
    }
    reservation.setStatus(newStatus);

    log.info("Reservation ({}) status {} -> {}", reservationId, oldStatus, newStatus);

    logEventService.logReservationStatusChangeEvent(Security.getUserDetails(), reservation, oldStatus);

    return reservationRepo.saveAndFlush(reservation);
  }

}
