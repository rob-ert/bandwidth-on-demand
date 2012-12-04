/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationArchive;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.domain.VirtualResourceGroup;
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
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import static nl.surfnet.bod.domain.ReservationStatus.CANCELLED;
import static nl.surfnet.bod.domain.ReservationStatus.FAILED;
import static nl.surfnet.bod.domain.ReservationStatus.REQUESTED;
import static nl.surfnet.bod.domain.ReservationStatus.RUNNING;
import static nl.surfnet.bod.domain.ReservationStatus.SCHEDULED;
import static nl.surfnet.bod.domain.ReservationStatus.SUCCEEDED;
import static nl.surfnet.bod.service.ReservationPredicatesAndSpecifications.forCurrentUser;
import static nl.surfnet.bod.service.ReservationPredicatesAndSpecifications.forVirtualResourceGroup;
import static nl.surfnet.bod.service.ReservationPredicatesAndSpecifications.specActiveReservations;
import static nl.surfnet.bod.service.ReservationPredicatesAndSpecifications.specByPhysicalPort;
import static nl.surfnet.bod.service.ReservationPredicatesAndSpecifications.specByVirtualPort;
import static nl.surfnet.bod.service.ReservationPredicatesAndSpecifications.specByVirtualPortAndManager;
import static nl.surfnet.bod.service.ReservationPredicatesAndSpecifications.specFilteredReservations;
import static nl.surfnet.bod.service.ReservationPredicatesAndSpecifications.specFilteredReservationsForManager;
import static nl.surfnet.bod.service.ReservationPredicatesAndSpecifications.specFilteredReservationsForUser;
import static nl.surfnet.bod.service.ReservationPredicatesAndSpecifications.specFilteredReservationsForVirtualResourceGroup;
import static nl.surfnet.bod.service.ReservationPredicatesAndSpecifications.specReservationsThatAreTimedOutAndTransitionally;
import static nl.surfnet.bod.service.ReservationPredicatesAndSpecifications.specReservationsThatCouldStart;

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
        if (reservationState == ReservationStatus.CANCEL_FAILED) {
          throw new RuntimeException("NMS Error during cancel of reservation: " + reservation.getReservationId());
        }
      }
    }

    Collection<ReservationArchive> reservationArchives = transformToReservationArchives(reservations);

    reservationArchiveRepo.save(reservationArchives);
    logEventService.logCreateEvent(Security.getUserDetails(), reservations);

    reservationRepo.delete(reservations);
    logEventService.logDeleteEvent(Security.getUserDetails(), "Canceled and archived", reservations);
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

  public long countActiveReservationsBetweenWithState(DateTime start, DateTime end, ReservationStatus state,
     Collection<String> adminGroups) {
    long count = 0;

    for (Long id : findReservationIdsStartBeforeAndEndInOrAfter(start, end)) {
      LogEvent logEvent = logEventService
          .findLatestStateChangeForReservationIdBeforeInAdminGroups(id, end, adminGroups);
      if ((logEvent != null) && (state == logEvent.getNewReservationStatus())) {
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

  public long countReservationsCreatedThroughChannelGUIInAdminGroups(DateTime start, DateTime end,
     Collection<String> adminGroups) {
    List<Long> reservationIds = logEventService.findReservationIdsCreatedBetweenWithStateInAdminGroups(start,
        end, ReservationStatus.REQUESTED, adminGroups);

    if (CollectionUtils.isEmpty(reservationIds)) {
      return 0;
    }

    return reservationRepo.count(ReservationPredicatesAndSpecifications.specReservationWithConnection(reservationIds));
  }

  public long countReservationsBetweenWhichHadStateInAdminGroups(DateTime start, DateTime end,
     Collection<String> adminGroups, ReservationStatus... states) {

    return logEventService.countDistinctDomainObjectId(LogEventPredicatesAndSpecifications
        .specForReservationBetweenForAdminGroupsWithStateIn(null, start, end, adminGroups, states));
  }

  public List<Long> findReservationIdsInAdminGroupsWhichHadStateBetween(DateTime start, DateTime end,
     Collection<String> adminGroups, ReservationStatus... states) {

    return logEventService.findDistinctDomainObjectIdsWithWhereClause(LogEventPredicatesAndSpecifications
        .specForReservationBetweenForAdminGroupsWithStateIn(null, start, end, adminGroups, states));
  }

  public long countReservationsWhichHadStateTransitionBetweenInAdminGroups(final DateTime start,
      final DateTime end, final ReservationStatus oldStatus, final ReservationStatus newStatus,Collection<String> adminGroups) {

    return logEventService.countDistinctDomainObjectId(LogEventPredicatesAndSpecifications
        .specStateChangeFromOldToNewForReservationIdInAdminGroupsBetween(oldStatus, newStatus, null, start, end,
            adminGroups));
  }

  public long countReservationsWithEndStateBetweenInAdminGroups(DateTime start, DateTime end,
     Collection<String> adminGroups, ReservationStatus... states) {

    if (states != null) {
      for (ReservationStatus status : states)
        Preconditions.checkArgument(status.isEndState());
    }

    return countReservationsBetweenWhichHadStateInAdminGroups(start, end, adminGroups, states);
  }

  public long countReservationsForIdsWithProtectionTypeAndCreatedBefore(List<Long> reservationIds,
      ProtectionType protectionType) {
    Preconditions.checkNotNull(protectionType);

    if (CollectionUtils.isEmpty(reservationIds)) {
      return 0;
    }

    return reservationRepo.count(ReservationPredicatesAndSpecifications.specReservationByProtectionTypeInIds(
        reservationIds, protectionType));
  }

  /**
   * Count the reservation requests which lead to a successfully created
   * reservation.
   * 
   * @param start
   *          {@link DateTime} start of period
   * @param end
   *          {@link DateTime} end of period
   * @param adminGroups
   *          Filter on these groups
   * @return long totalAmount
   */
  public long countSuccesfullReservationRequestsInAdminGroups(final DateTime start, final DateTime end,
     Collection<String> adminGroups) {
    return findSuccessfullReservationRequestsInAdminGroups(start, end, adminGroups).size();
  }

  public List<Long> findSuccessfullReservationRequestsInAdminGroups(DateTime start, DateTime end,
     Collection<String> adminGroups) {
    Set<Long> reservationIds = new HashSet<>();

    // ReservationRequests
    reservationIds.addAll(findReservationIdsInAdminGroupsWhichHadStateBetween(start, end, adminGroups,
        ReservationStatus.RESERVED));

    reservationIds.addAll(logEventService.findStateChangeFromOldToNewForReservationIdInAdminGroupsBetween(start, end,
        ReservationStatus.REQUESTED, ReservationStatus.AUTO_START, adminGroups));

    return Lists.newArrayList(reservationIds);
  }

  /**
   * Count the reservation requests which did not result in a reservation
   * 
   * @param start
   *          {@link DateTime} start of period
   * @param end
   *          {@link DateTime} end of period
   * @return long totalAmount
   */
  public long countFailedReservationRequestsInAdminGroups(final DateTime start, final DateTime end,
     Collection<String> adminGroups) {

    long failedRequests = countReservationsWithEndStateBetweenInAdminGroups(start, end, adminGroups,
        ReservationStatus.NOT_ACCEPTED);

    return failedRequests += countReservationsWhichHadStateTransitionBetweenInAdminGroups(start, end, REQUESTED,
        FAILED, adminGroups);
  }

  /**
   * Counts the amount of reservations in the between the given Start and end
   * that are SUCCEEDED or transferred from SCHEDULED -> CANCEL or transferred
   * from RUNNING ->CANCEL
   * 
   * @param start
   *          {@link DateTime} start of period
   * @param end
   *          {@link DateTime} end of period
   * @return long totalAmount
   */
  public long countRunningReservationsInAdminGroupsSucceeded(final DateTime start, final DateTime end,
     Collection<String> adminGroups) {
    List<Long> reservationIdsInPeriod = findReservationIdsStartBeforeAndEndInOrAfter(start, end);

    final Specification<LogEvent> specSucceeded = LogEventPredicatesAndSpecifications
        .specForReservationBetweenForAdminGroupsWithStateIn(reservationIdsInPeriod, start, end, adminGroups, SUCCEEDED);

    final Specification<LogEvent> specScheduledToCancelled = LogEventPredicatesAndSpecifications
        .specStateChangeFromOldToNewForReservationIdInAdminGroupsBetween(ReservationStatus.SCHEDULED, CANCELLED,
            reservationIdsInPeriod, start, end, adminGroups);

    final Specification<LogEvent> specRunningToCancelled = LogEventPredicatesAndSpecifications
        .specStateChangeFromOldToNewForReservationIdInAdminGroupsBetween(RUNNING, CANCELLED, reservationIdsInPeriod,
            start, end, adminGroups);

    final Specifications<LogEvent> specRunningReservations = Specifications.where(specSucceeded).or(
        specScheduledToCancelled).or(specRunningToCancelled);

    return logEventService.countDistinctDomainObjectId(specRunningReservations);
  }

  /**
   * Counts the amount of reservations in the between the given Start and end
   * that transferred from RUNNING -> FAILED or transferred from SCHEDULED ->
   * FAILED.
   * 
   * @param start
   *          {@link DateTime} start of period
   * @param end
   *          {@link DateTime} end of period
   * @param adminGroups
   * @return long totalAmount
   */
  public long countRunningReservationsInAdminGroupsFailed(final DateTime start, final DateTime end,
     Collection<String> adminGroups) {
    List<Long> reservationIdsInPeriod = findReservationIdsStartBeforeAndEndInOrAfter(start, end);

    final Specification<LogEvent> specRunningToFailed = LogEventPredicatesAndSpecifications
        .specStateChangeFromOldToNewForReservationIdInAdminGroupsBetween(RUNNING, FAILED, reservationIdsInPeriod,
            start, end, adminGroups);

    final Specification<LogEvent> specScheduledToFailed = LogEventPredicatesAndSpecifications
        .specStateChangeFromOldToNewForReservationIdInAdminGroupsBetween(SCHEDULED, FAILED, reservationIdsInPeriod,
            start, end, adminGroups);

    final Specifications<LogEvent> specRunningReservations = Specifications.where(specRunningToFailed).or(
        specScheduledToFailed);

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
    logEventService.logCreateEvent(Security.getUserDetails(), reservation);

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

  public List<Long> findReservationIdsBeforeInAdminGroupsWithState(DateTime before,Collection<String> adminGroups,
      ReservationStatus... states) {
    final Specification<LogEvent> whereClause = LogEventPredicatesAndSpecifications
        .specForReservationBeforeInAdminGroupsWithStateIn(null, before, adminGroups, states);

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

    logEventService.logReservationStatusChangeEvent(Security.getUserDetails(), freshRes, oldStatus);

    return reservationRepo.saveAndFlush(freshRes);
  }

}
