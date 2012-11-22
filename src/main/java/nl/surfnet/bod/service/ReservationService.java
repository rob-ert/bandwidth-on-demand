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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static nl.surfnet.bod.domain.ReservationStatus.AUTO_START;
import static nl.surfnet.bod.domain.ReservationStatus.CANCELLED;
import static nl.surfnet.bod.domain.ReservationStatus.RUNNING;
import static nl.surfnet.bod.domain.ReservationStatus.SUCCEEDED;
import static nl.surfnet.bod.service.ReservationPredicatesAndSpecifications.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.Future;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nl.surfnet.bod.domain.*;
import nl.surfnet.bod.event.LogEvent;
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
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.*;

@Service
@Transactional
public class ReservationService extends AbstractFullTextSearchService<Reservation> {

  private final ObjectMapper mapper = new ObjectMapper();

  private final Function<Reservation, ReservationArchive> TO_RESERVATION_ARCHIVE = new Function<Reservation, ReservationArchive>() {
    @Override
    public ReservationArchive apply(Reservation reservation) {
      final ReservationArchive reservationArchive = new ReservationArchive();
      reservationArchive.setReservationPrimaryKey(reservation.getId());
      final StringWriter writer = new StringWriter();
      try {
        mapper.writeValue(writer, reservation);
      }
      catch (IOException e) {
        // need to throw or we'll lose reservations
        throw new RuntimeException(e);
      }
      reservationArchive.setReservationAsJson(writer.toString());
      return reservationArchive;
    }
  };

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Resource
  private ReservationRepo reservationRepo;

  @Resource
  private ReservationArchiveRepo reservationArchiveRepo;

  @Resource
  private NbiClient nbiClient;

  @Resource
  private ReservationToNbi reservationToNbi;

  @PersistenceContext
  private EntityManager entityManager;

  @Resource
  private LogEventService logEventService;

  public ReservationService() {
    mapper.setVisibilityChecker(mapper.getSerializationConfig().getDefaultVisibilityChecker().withFieldVisibility(
        JsonAutoDetect.Visibility.ANY).withGetterVisibility(JsonAutoDetect.Visibility.NONE).withSetterVisibility(
        JsonAutoDetect.Visibility.NONE).withCreatorVisibility(JsonAutoDetect.Visibility.NONE).withIsGetterVisibility(
        JsonAutoDetect.Visibility.NONE));
  }

  void alignConnectionWithReservation(Reservation reservation) {
    if (reservation.isNSICreated()) {
      Connection connection = reservation.getConnection().get();
      connection.setStartTime(reservation.getStartDateTime());
      connection.setEndTime(reservation.getEndDateTime());
    }
  }

  /**
   * Cancels a reservation if the current user has the correct role and the
   * reservation is allowed to be deleted depending on its state. Updates the
   * state of the reservation.
   *
   * @param reservation
   *          {@link Reservation} to delete
   * @return the future with the resulting reservation, or null if delete is not
   *         allowed.
   */
  public Optional<Future<Long>> cancel(Reservation reservation, RichUserDetails user) {
    return cancelWithReason(reservation, "Cancelled by " + user.getDisplayName(), user);
  }

  public void cancelAndArchiveReservations(final List<Reservation> reservations, RichUserDetails user) {
    for (final Reservation reservation : reservations) {
      if (isDeleteAllowedForUserOnly(reservation, user.getSelectedRole()).isAllowed()
          && reservation.getStatus().isTransitionState()) {
        final ReservationStatus reservationState = nbiClient.cancelReservation(reservation.getReservationId());
        reservation.setStatus(reservationState);
        if(reservationState == ReservationStatus.CANCEL_FAILED){
          throw new RuntimeException("NMS Error during cancel of reservation: "+reservation.getReservationId());
        }
      }
    }

    Collection<ReservationArchive> reservationArchives = transformToReservationArchives(reservations);

    reservationArchiveRepo.save(reservationArchives);
    logEventService.logCreateEvent(Security.getUserDetails(), reservations, "Archived due to cancel");

    reservationRepo.delete(reservations);
    logEventService.logDeleteEvent(Security.getUserDetails(), reservations, "Canceled and archived");
  }

  public Optional<Future<Long>> cancelWithReason(Reservation reservation, String cancelReason, RichUserDetails user) {
    return cancelWithReason(reservation, cancelReason, user, Optional.<NsiRequestDetails> absent());
  }

  public Optional<Future<Long>> cancelWithReason(Reservation reservation, String cancelReason, RichUserDetails user,
      Optional<NsiRequestDetails> requestDetails) {

    if (isDeleteAllowed(reservation, user.getSelectedRole()).isAllowed() && reservation.getStatus().isTransitionState()) {
      return Optional.of(reservationToNbi.asyncTerminate(reservation.getId(), cancelReason, requestDetails));
    }

    log.info("Not allowed to cancel reservation {} with state {}", reservation.getName(), reservation.getStatus());

    return Optional.absent();
  }

  private void correctStart(Reservation reservation) {
    if (reservation.getStartDateTime() == null || reservation.getStartDateTime().isBeforeNow()) {
      reservation.setStartDateTime(DateTime.now().plusMinutes(1));
    }
  }

  public long count() {
    return reservationRepo.count();
  }

  public long countActiveReservationsBetweenWithStatusIn(DateTime start, DateTime end, ReservationStatus... states) {
    long count = 0;

    for (Long id : findReservationIdsStartBeforeAndEndInOrAfter(start, end)) {
      LogEvent logEvent = logEventService.findLatestStateChangeForReservationIdBeforeWithStateIn(id, end, states);
      if (logEvent != null) {
        count++;
      }
    }

    return count;
  }

  public long countActiveReservationsByVirtualPorts(List<VirtualPort> virtualPorts) {
    if (CollectionUtils.isEmpty(virtualPorts)) {
      return 0;
    }

    final Specification<Reservation> whereClause = ReservationPredicatesAndSpecifications
        .specActiveByVirtualPorts(virtualPorts);
    return reservationRepo.count(whereClause);
  }

  public long countActiveReservationsForGroup(VirtualResourceGroup group) {
    return countForFilterAndVirtualResourceGroup(ReservationFilterViewFactory.ACTIVE, group);
  }

  public long countActiveReservationsForPhysicalPort(final PhysicalPort port) {
    return reservationRepo.count(Specifications.where(specByPhysicalPort(port)).and(specActiveReservations()));
  }

  public long countAllEntriesUsingFilter(final ReservationFilterView filter) {
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

  public long countForPhysicalPort(final PhysicalPort port) {
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

  public long countReservationsForNocCreatedThroughChannelGUI(DateTime start, DateTime end) {
    List<Long> reservationIds = logEventService.findReservationIdsCreatedBetweenForNocWithState(start, end,
        ReservationStatus.REQUESTED);

    if (CollectionUtils.isEmpty(reservationIds)) {
      return 0;
    }

    return reservationRepo.count(ReservationPredicatesAndSpecifications.specReservationWithConnection(reservationIds));
  }

  public long countReservationsForNocWhichHadStateBetween(DateTime start, DateTime end, ReservationStatus... states) {

    return logEventService.countDistinctDomainObjectId(LogEventPredicatesAndSpecifications
        .specLatestStateForReservationBetweenWithStateIn(Optional.<List<Long>> absent(), start, end, states));
  }

  public long countReservationsForNocWhichHadStateTransitionBetween(final DateTime start, final DateTime end,
      final ReservationStatus oldStatus, final ReservationStatus newStatus) {

    return logEventService.countDistinctDomainObjectId(LogEventPredicatesAndSpecifications
        .specStateChangeFromOldToNewForReservationIdBetween(oldStatus, newStatus, Optional.<List<Long>> absent(),
            start, end));
  }

  public long countReservationsForNocWithEndStateBetween(DateTime start, DateTime end, ReservationStatus... states) {

    if (states != null) {
      for (ReservationStatus status : states)
        Preconditions.checkArgument(status.isEndState());
    }

    return countReservationsForNocWhichHadStateBetween(start, end, states);
  }

  public long countReservationsNocForIdsWithProtectionTypeAndCreatedBefore(List<Long> reservationIds,
      ProtectionType protectionType) {
    Preconditions.checkNotNull(protectionType);

    if (CollectionUtils.isEmpty(reservationIds)) {
      return 0;
    }

    return reservationRepo.count(ReservationPredicatesAndSpecifications.specReservationByProtectionTypeInIds(
        reservationIds, protectionType));
  }

  /**
   * Counts the amount of reservations in the between the given Start and end
   * that are SUCCEEDED or transferred from AUTO_START -> CANCEL or transferred
   * from RUNNING ->CANCEL
   *
   * @param start
   *          {@link DateTime} start of period
   * @param end
   *          {@link DateTime} end of period
   * @return long totalAmount
   */
  public long countRunningReservationsSucceeded(DateTime start, DateTime end) {
    Optional<List<Long>> reservationIdsInPeriod = Optional
        .<List<Long>> fromNullable(findReservationIdsStartBeforeAndEndInOrAfter(start, end));

    final Specification<LogEvent> specSucceeded = LogEventPredicatesAndSpecifications
        .specLatestStateForReservationBetweenWithStateIn(reservationIdsInPeriod, start, end, SUCCEEDED);

    final Specification<LogEvent> specAutoStartToCancelled = LogEventPredicatesAndSpecifications
        .specStateChangeFromOldToNewForReservationIdBetween(AUTO_START, CANCELLED, reservationIdsInPeriod, start, end);

    final Specification<LogEvent> specRunningToCancelled = LogEventPredicatesAndSpecifications
        .specStateChangeFromOldToNewForReservationIdBetween(RUNNING, CANCELLED, reservationIdsInPeriod, start, end);

    final Specifications<LogEvent> specRunningReservations = Specifications.where(specSucceeded).or(
        specAutoStartToCancelled).or(specRunningToCancelled);

    return logEventService.countDistinctDomainObjectId(specRunningReservations);
  }

  /**
   * Creates a {@link Reservation} which is auto provisioned
   *
   * @param reservation
   * @See {@link #create(Reservation)}
   */
  public Future<Long> create(Reservation reservation) {
    return create(reservation, true, Optional.<NsiRequestDetails> absent());
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
  public Future<Long> create(Reservation reservation, boolean autoProvision,
      Optional<NsiRequestDetails> nsiRequestDetails) {
    checkState(reservation.getSourcePort().getVirtualResourceGroup().equals(reservation.getVirtualResourceGroup()));
    checkState(reservation.getDestinationPort().getVirtualResourceGroup().equals(reservation.getVirtualResourceGroup()));

    correctStart(reservation);
    stripSecondsAndMillis(reservation);
    alignConnectionWithReservation(reservation);

    // make sure the reservation is written to the database before we call the
    // async reserve
    reservation = reservationRepo.saveAndFlush(reservation);

    // Log event after creation, so the ID is set by hibernate
    logEventService.logCreateEvent(Security.getUserDetails(), reservation, "Created reservation with name: "
        + reservation.getName());

    return reservationToNbi.asyncReserve(reservation.getId(), autoProvision, nsiRequestDetails);
  }

  public Reservation find(Long id) {
    return reservationRepo.findOne(id);
  }

  public Collection<Reservation> findActiveByPhysicalPort(final PhysicalPort port) {
    return reservationRepo.findAll(Specifications.where(specByPhysicalPort(port)).and(specActiveReservations()));
  }

  public Collection<Reservation> findAllActiveByVirtualPort(final VirtualPort port) {
    return reservationRepo.findAll(Specifications.where(specByVirtualPort(port)).and(specActiveReservations()));
  }

  public Collection<Reservation> findAllActiveByVirtualPortForManager(final VirtualPort port, final RichUserDetails user) {
    return reservationRepo.findAll(Specifications.where(specByVirtualPort(port)).and(specActiveReservations()).and(
        specByVirtualPortAndManager(port, user)));
  }

  public List<Reservation> findAllEntriesUsingFilter(final ReservationFilterView filter, int firstResult,
      int maxResults, Sort sort) {

    return reservationRepo.findAll(specFilteredReservations(filter),
        new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();
  }

  public Reservation findByConnectionId(String connectionId) {
    return reservationRepo.findByConnectionConnectionId(connectionId);
  }

  public Reservation findByReservationId(final String reservationId) {
    return reservationRepo.findByReservationId(reservationId);
  }

  public List<Reservation> findBySourcePortOrDestinationPort(VirtualPort virtualPortA, VirtualPort virtualPortB) {
    return reservationRepo.findBySourcePortOrDestinationPort(virtualPortA, virtualPortB);
  }

  public Collection<Reservation> findByVirtualPort(VirtualPort port) {
    return reservationRepo.findBySourcePortOrDestinationPort(port, port);
  }

  public List<Reservation> findEntries(int firstResult, int maxResults, Sort sort) {
    final RichUserDetails user = Security.getUserDetails();

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

  public List<Reservation> findEntriesForUserUsingFilter(final RichUserDetails user,
      final ReservationFilterView filter, int firstResult, int maxResults, Sort sort) {

    if (user.getUserGroupIds().isEmpty()) {
      return Collections.emptyList();
    }

    final List<Reservation> content = reservationRepo.findAll(specFilteredReservationsForUser(filter, user),
        new PageRequest(firstResult / maxResults, maxResults, sort)).getContent();

    return content;
  }

  public List<Long> findIdsForManagerUsingFilter(RichUserDetails manager, ReservationFilterView filter) {
    return reservationRepo.findIdsWithWhereClause(specFilteredReservationsForManager(filter, manager));
  }

  public List<Long> findIdsForNocUsingFilter(ReservationFilterView filter) {
    return reservationRepo.findIdsWithWhereClause(specFilteredReservations(filter));
  }

  public List<Long> findIdsForUserUsingFilter(RichUserDetails user, ReservationFilterView filter) {
    return reservationRepo.findIdsWithWhereClause(specFilteredReservationsForUser(filter, user));
  }

  public List<Long> findReservationIdsBeforeWithLatestState(DateTime before, ReservationStatus... states) {
    final Specification<LogEvent> whereClause = LogEventPredicatesAndSpecifications
        .specLatestStateForReservationBeforeWithStateIn(Optional.<List<Long>> absent(), before, states);

    return logEventService.findDistinctDomainObjectIdsWithWhereClause(whereClause);
  }

  public List<Long> findReservationIdsStartBeforeAndEndInOrAfter(DateTime start, DateTime end) {

    final Specification<Reservation> whereClause = ReservationPredicatesAndSpecifications
        .specReservationStartBeforeAndEndInOrAfter(start, end);

    return reservationRepo.findIdsWithWhereClause(whereClause);
  }

  /**
   * Finds all reservations which start or ends on the given dateTime and have a
   * status which can still change its status.
   *
   * @param dateTime
   *          {@link LocalDateTime} to search for
   * @return list of found Reservations
   */
  public Collection<Reservation> findReservationsToPoll(DateTime dateTime) {
    Set<Reservation> reservations = Sets.newHashSet();
    reservations.addAll(reservationRepo.findAll(specReservationsThatCouldStart(dateTime)));
    reservations.addAll(reservationRepo.findAll(specReservationsThatAreTimedOutAndTransitionally(dateTime)));
    reservations.addAll(findReservationWithStatus(RUNNING));

    return reservations;
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
        }).toImmutableList();

    return Ordering.natural().sortedCopy(years);
  }

  @Override
  protected EntityManager getEntityManager() {
    return entityManager;
  }

  @VisibleForTesting
  public final ObjectMapper getObjectMapper() {
    return mapper;
  }

  public ReservationStatus getStatus(Reservation reservation) {
    final Optional<ReservationStatus> optionalReservationStatus = nbiClient.getReservationStatus(reservation
        .getReservationId());
    if (optionalReservationStatus.isPresent()) {
      return optionalReservationStatus.get();
    }
    return reservation.getStatus();
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
  public ElementActionView isDeleteAllowed(Reservation reservation, BodRole role) {
    if (!reservation.getStatus().isDeleteAllowed()) {
      return new ElementActionView(false, "reservation_state_transition_not_allowed");
    }

    return isDeleteAllowedForUserOnly(reservation, role);
  }

  private ElementActionView isDeleteAllowedForUserOnly(Reservation reservation, BodRole role) {
    if (role.isNocRole()) {
      return new ElementActionView(true, "label_cancel");
    }
    else if (role.isManagerRole()
        && (reservation.getSourcePort().getPhysicalResourceGroup().getId().equals(
            role.getPhysicalResourceGroupId().get()) || reservation.getDestinationPort().getPhysicalResourceGroup()
            .getId().equals(role.getPhysicalResourceGroupId().get()))) {
      return new ElementActionView(true, "label_cancel");
    }
    else if (role.isUserRole() && Security.isUserMemberOf(reservation.getVirtualResourceGroup())) {
      return new ElementActionView(true, "label_cancel");
    }

    return new ElementActionView(false, "reservation_cancel_user_has_no_rights");
  }

  public ElementActionView isEditAllowed(Reservation reservation, BodRole role) {
    if (role.isNocRole()) {
      return new ElementActionView(false, "reservation_edit_user_has_no_rights");
    }
    else if (role.isManagerRole()) {
      return new ElementActionView(false, "reservation_copy_user_has_no_rights");
    }
    else if (role.isUserRole() && Security.isUserMemberOf(reservation.getVirtualResourceGroup())) {
      return new ElementActionView(true, "label_copy");
    }

    return new ElementActionView(false, "reservation_edit_user_has_no_rights");
  }

  /**
   * Activates an existing reservation;
   *
   * @param reservation
   *          {@link Reservation} to activate
   * @return true if the reservation was successfully activated, false otherwise
   */
  public void provision(Reservation reservation, Optional<NsiRequestDetails> requestDetails) {
    checkNotNull(reservation);

    reservationToNbi.asyncProvision(reservation.getId(), requestDetails);
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
  Collection<ReservationArchive> transformToReservationArchives(final List<Reservation> reservations) {
    return Collections2.transform(reservations, TO_RESERVATION_ARCHIVE);
  }

  public Reservation updateStatus(Reservation reservation, ReservationStatus newStatus) {
    ReservationStatus oldStatus = reservation.getStatus();
    Reservation freshRes = find(reservation.getId());

    freshRes.setStatus(newStatus);

    log.info("Reservation ({}) status {} -> {}", reservation.getId(), reservation.getStatus(), newStatus);

    logEventService.logUpdateEvent(Security.getUserDetails(), freshRes,
        LogEvent.getStateChangeMessage(freshRes, oldStatus), Optional.of(oldStatus));

    return reservationRepo.saveAndFlush(freshRes);
  }

}
