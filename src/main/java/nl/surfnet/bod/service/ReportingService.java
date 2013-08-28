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

import static nl.surfnet.bod.domain.ReservationStatus.AUTO_START;
import static nl.surfnet.bod.domain.ReservationStatus.CANCELLED;
import static nl.surfnet.bod.domain.ReservationStatus.CANCEL_FAILED;
import static nl.surfnet.bod.domain.ReservationStatus.FAILED;
import static nl.surfnet.bod.domain.ReservationStatus.NOT_ACCEPTED;
import static nl.surfnet.bod.domain.ReservationStatus.PASSED_END_TIME;
import static nl.surfnet.bod.domain.ReservationStatus.REQUESTED;
import static nl.surfnet.bod.domain.ReservationStatus.RESERVED;
import static nl.surfnet.bod.domain.ReservationStatus.RUNNING;
import static nl.surfnet.bod.domain.ReservationStatus.SCHEDULED;
import static nl.surfnet.bod.domain.ReservationStatus.SUCCEEDED;
import static nl.surfnet.bod.domain.ReservationStatus.TRANSITION_STATES;
import static nl.surfnet.bod.domain.ReservationStatus.TRANSITION_STATES_AS_ARRAY;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.repo.ReservationRepo;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.view.ReservationReportView;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@Transactional
public class ReportingService {

  @Resource private LogEventService logEventService;
  @Resource private ReservationRepo reservationRepo;
  @Resource private VirtualResourceGroupService virtualResourceGroupService;

  public ReservationReportView determineReportForUser(Interval interval, RichUserDetails user) {
    return determineReport(interval, virtualResourceGroupService.determineAdminGroupsForUser(user));
  }

  public ReservationReportView determineReportForAdmin(Interval interval, BodRole managerRole) {
    return determineReport(interval, ImmutableList.of(managerRole.getAdminGroup().get()));
  }

  public ReservationReportView determineReportForNoc(Interval interval) {
    return determineReport(interval, Collections.<String> emptyList());
  }

  private ReservationReportView determineReport(Interval interval, Collection<String> adminGroups) {
    ReservationReportView reservationReport = new ReservationReportView(interval.getStart(), interval.getEnd());
    determineReservationRequestsForGroups(reservationReport, adminGroups);
    determineReservationsInAdminGroupsForProtectionType(reservationReport, adminGroups);
    determineActiveRunningReservations(reservationReport, adminGroups);

    return reservationReport;
  }

  @VisibleForTesting
  void determineReservationRequestsForGroups(ReservationReportView reservationReport, Collection<String> adminGroups) {
    final DateTime start = reservationReport.getPeriodStart();
    final DateTime end = reservationReport.getPeriodEnd();

    // ReservationRequests
    reservationReport.setAmountRequestsCreatedSucceeded(findSuccessfulReservationRequestsInAdminGroups(start, end, adminGroups).size());
    reservationReport.setAmountRequestsCreatedFailed(countFailedReservationRequestsInAdminGroups(
        start, end, adminGroups));

    // No modify requests yet, init on zero
    reservationReport.setAmountRequestsModifiedSucceeded(0L);
    reservationReport.setAmountRequestsModifiedFailed(0L);

    reservationReport.setAmountRequestsCancelSucceeded(countReservationsBetweenWhichHadStateInAdminGroups(start, end, adminGroups, CANCELLED));
    reservationReport.setAmountRequestsCancelFailed(countReservationsBetweenWhichHadStateInAdminGroups(start, end, adminGroups, CANCEL_FAILED));

    // Actual Reservations by channel
    reservationReport.setAmountRequestsThroughNSI(
        countReservationsCreatedThroughChannelNSIInAdminGroups(start, end, adminGroups)
        + countReservationsCancelledThroughChannelNSIInAdminGroups(start, end, adminGroups));

    reservationReport.setAmountRequestsThroughGUI(
        countReservationsCreatedThroughChannelGUIInAdminGroups(start, end, adminGroups)
        + countReservationsCancelledThroughChannelGUInAdminGroups(start, end, adminGroups));
  }

  @VisibleForTesting
  void determineReservationsInAdminGroupsForProtectionType(ReservationReportView reservationReport,
      Collection<String> adminGroups) {
    final DateTime start = reservationReport.getPeriodStart();
    final DateTime end = reservationReport.getPeriodEnd();

    Set<Long> reservationIds = new HashSet<>();

    for (Long id : findReservationIdsBeforeOrOnInAdminGroupsWithState(start, adminGroups, TRANSITION_STATES_AS_ARRAY)) {
      LogEvent logEvent = logEventService.findLatestStateChangeForReservationIdBeforeInAdminGroups(id, start, adminGroups);
      if (TRANSITION_STATES.contains(logEvent.getNewReservationStatus())) {
        reservationIds.add(id);
      }
    }
    reservationIds.addAll(findSuccessfulReservationRequestsInAdminGroups(start, end, adminGroups));

    List<Long> reservationIdList = Lists.newArrayList(reservationIds);

    reservationReport.setAmountReservationsProtected(countReservationsForIdsWithProtectionType(reservationIdList, ProtectionType.PROTECTED));
    reservationReport.setAmountReservationsUnprotected(countReservationsForIdsWithProtectionType(reservationIdList, ProtectionType.UNPROTECTED));
    reservationReport.setAmountReservationsRedundant(countReservationsForIdsWithProtectionType(reservationIdList, ProtectionType.REDUNDANT));
  }

  @VisibleForTesting
  void determineActiveRunningReservations(ReservationReportView reservationReport, Collection<String> adminGroups) {
    final DateTime start = reservationReport.getPeriodStart();
    final DateTime end = reservationReport.getPeriodEnd();

    List<Long> reservationIdsInPeriod = ImmutableList.copyOf(findReservationIdsStartBeforeAndEndInOrAfter(start, end));

    reservationReport.setAmountRunningReservationsSucceeded(countRunningReservationsInAdminGroupsSucceeded(reservationIdsInPeriod, start, end, adminGroups));
    reservationReport.setAmountRunningReservationsFailed(countRunningReservationsInAdminGroupsFailed(reservationIdsInPeriod, start, end, adminGroups));
    reservationReport.setAmountRunningReservationsStillRunning(countActiveReservationsBetweenWithState(reservationIdsInPeriod, start, end, RUNNING, adminGroups));
    reservationReport.setAmountRunningReservationsStillScheduled(countActiveReservationsBetweenWithState(reservationIdsInPeriod, start, end, SCHEDULED, adminGroups));
    reservationReport.setAmountRunningReservationsNeverProvisioned(countReservationsBetweenWhichHadStateInAdminGroups(start, end, adminGroups, PASSED_END_TIME));
  }

  @VisibleForTesting
  List<Long> findReservationIdsStartBeforeAndEndInOrAfter(DateTime start, DateTime end) {
    Specification<Reservation> whereClause = ReservationPredicatesAndSpecifications
        .specReservationStartBeforeAndEndInOrAfter(start, end);

    return reservationRepo.findIdsWithWhereClause(whereClause, Optional.<Sort> absent());
  }

  @VisibleForTesting
  List<Long> findSuccessfulReservationRequestsInAdminGroups(DateTime start, DateTime end,
      Collection<String> adminGroups) {
    Set<Long> reservationIds = new HashSet<>();

    // ReservationRequests
    reservationIds.addAll(findReservationIdsInAdminGroupsWhichHadStateBetween(start, end, adminGroups, RESERVED, AUTO_START));

    return Lists.newArrayList(reservationIds);
  }

  @VisibleForTesting
  List<Long> findReservationIdsInAdminGroupsWhichHadStateBetween(DateTime start, DateTime end,
      Collection<String> adminGroups, ReservationStatus... states) {
    return logEventService.findDistinctDomainObjectIdsWithWhereClause(LogEventPredicatesAndSpecifications
        .specForReservationBetweenForAdminGroupsWithStateIn(null, start, end, adminGroups, states));
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
  @VisibleForTesting
  long countFailedReservationRequestsInAdminGroups(DateTime start, DateTime end, Collection<String> adminGroups) {
    return (countReservationsBetweenWhichHadStateInAdminGroups(start, end, adminGroups, NOT_ACCEPTED)
        + countReservationsWhichHadStateTransitionBetweenInAdminGroups(start, end, REQUESTED, FAILED, adminGroups));
  }

  @VisibleForTesting
  long countReservationsBetweenWhichHadStateInAdminGroups(DateTime start, DateTime end,
      Collection<String> adminGroups, ReservationStatus... states) {
    return logEventService.countDistinctDomainObjectId(LogEventPredicatesAndSpecifications
        .specForReservationBetweenForAdminGroupsWithStateIn(null, start, end, adminGroups, states));
  }

  @VisibleForTesting
  long countReservationsWhichHadStateTransitionBetweenInAdminGroups(DateTime start, DateTime end,
      ReservationStatus oldStatus, ReservationStatus newStatus, Collection<String> adminGroups) {
    return logEventService.countDistinctDomainObjectId(LogEventPredicatesAndSpecifications
        .specStateChangeFromOldToNewForReservationIdInAdminGroupsBetween(oldStatus, newStatus, null, start, end,
            adminGroups));
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
   * @param reservationIdsInPeriod
   * @return long totalAmount
   */
  @VisibleForTesting
  long countRunningReservationsInAdminGroupsSucceeded(List<Long> reservationIdsInPeriod, DateTime start, DateTime end, Collection<String> adminGroups) {
    Specification<LogEvent> specSucceeded = LogEventPredicatesAndSpecifications
        .specForReservationBetweenForAdminGroupsWithStateIn(reservationIdsInPeriod, start, end, adminGroups, SUCCEEDED);

    Specification<LogEvent> specScheduledToCancelled = LogEventPredicatesAndSpecifications
        .specStateChangeFromOldToNewForReservationIdInAdminGroupsBetween(SCHEDULED, CANCELLED,
            reservationIdsInPeriod, start, end, adminGroups);

    Specification<LogEvent> specRunningToCancelled = LogEventPredicatesAndSpecifications
        .specStateChangeFromOldToNewForReservationIdInAdminGroupsBetween(RUNNING, CANCELLED, reservationIdsInPeriod,
            start, end, adminGroups);

    Specifications<LogEvent> specRunningReservations = Specifications.where(specSucceeded).or(
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
   * @param reservationIdsInPeriod
   * @return long totalAmount
   */
  @VisibleForTesting
  long countRunningReservationsInAdminGroupsFailed(List<Long> reservationIdsInPeriod, DateTime start, DateTime end, Collection<String> adminGroups) {
    Specification<LogEvent> specRunningToFailed = LogEventPredicatesAndSpecifications
        .specStateChangeFromOldToNewForReservationIdInAdminGroupsBetween(RUNNING, FAILED, reservationIdsInPeriod,
            start, end, adminGroups);

    Specification<LogEvent> specScheduledToFailed = LogEventPredicatesAndSpecifications
        .specStateChangeFromOldToNewForReservationIdInAdminGroupsBetween(SCHEDULED, FAILED, reservationIdsInPeriod,
            start, end, adminGroups);

    Specifications<LogEvent> specRunningReservations = Specifications.where(specRunningToFailed).or(
        specScheduledToFailed);

    return logEventService.countDistinctDomainObjectId(specRunningReservations);
  }

  @VisibleForTesting
  long countReservationsCreatedThroughChannelNSIInAdminGroups(DateTime start, DateTime end, Collection<String> adminGroups) {
    List<Long> reservationIds = logEventService.findReservationsIdsCreatedBetweenWithOldStateInAdminGroups(start, end,
        REQUESTED, adminGroups);

    if (CollectionUtils.isEmpty(reservationIds)) {
      return 0;
    }

    return reservationRepo.count(ReservationPredicatesAndSpecifications.specReservationWithConnection(reservationIds));
  }

  @VisibleForTesting
  long countReservationsCancelledThroughChannelNSIInAdminGroups(DateTime start, DateTime end, Collection<String> adminGroups) {
    List<Long> reservationIds = logEventService.findReservationIdsCreatedBetweenWithStateInAdminGroups(start, end,
        adminGroups, CANCELLED, CANCEL_FAILED);

    if (CollectionUtils.isEmpty(reservationIds)) {
      return 0;
    }

    return reservationRepo.count(ReservationPredicatesAndSpecifications.specReservationWithConnection(reservationIds));
  }

  @VisibleForTesting
  long countReservationsCreatedThroughChannelGUIInAdminGroups(DateTime start, DateTime end, Collection<String> adminGroups) {
    List<Long> reservationIds = logEventService.findReservationsIdsCreatedBetweenWithOldStateInAdminGroups(start, end, REQUESTED, adminGroups);

    if (CollectionUtils.isEmpty(reservationIds)) {
      return 0;
    }

    long withConnection = reservationRepo.count(ReservationPredicatesAndSpecifications.specReservationWithConnection(reservationIds));

    return reservationIds.size() - withConnection;
  }

  @VisibleForTesting
  long countReservationsCancelledThroughChannelGUInAdminGroups(DateTime start, DateTime end, Collection<String> adminGroups) {
    List<Long> reservationIds = logEventService.findReservationIdsCreatedBetweenWithStateInAdminGroups(start, end,
        adminGroups, CANCELLED, CANCEL_FAILED);

    if (CollectionUtils.isEmpty(reservationIds)) {
      return 0;
    }

    long withConnection = reservationRepo.count(ReservationPredicatesAndSpecifications.specReservationWithConnection(reservationIds));

    return reservationIds.size() - withConnection;
  }

  @VisibleForTesting
  long countReservationsForIdsWithProtectionType(List<Long> reservationIds, ProtectionType protectionType) {
    Preconditions.checkNotNull(protectionType);

    if (CollectionUtils.isEmpty(reservationIds)) {
      return 0;
    }

    return reservationRepo.count(ReservationPredicatesAndSpecifications.specReservationByProtectionTypeInIds(
        reservationIds, protectionType));
  }

  @VisibleForTesting
  long countActiveReservationsBetweenWithState(List<Long> reservationIdsInPeriod, DateTime start,
      DateTime end, ReservationStatus state, Collection<String> adminGroups) {
    long count = 0;

    for (Long id : reservationIdsInPeriod) {
      LogEvent logEvent = logEventService.findLatestStateChangeForReservationIdBeforeInAdminGroups(id, end, adminGroups);
      if ((logEvent != null) && (state == logEvent.getNewReservationStatus())) {
        count++;
      }
    }

    return count;
  }

  @VisibleForTesting
  List<Long> findReservationIdsBeforeOrOnInAdminGroupsWithState(DateTime before, Collection<String> adminGroups,
      ReservationStatus... states) {
    return logEventService.findDistinctDomainObjectIdsWithWhereClause(LogEventPredicatesAndSpecifications
        .specForReservationBeforeInAdminGroupsWithStateIn(Optional.<Long> absent(), before, adminGroups, states));
  }
}
