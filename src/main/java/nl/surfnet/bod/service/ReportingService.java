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

import static nl.surfnet.bod.domain.ReservationStatus.RUNNING;
import static nl.surfnet.bod.domain.ReservationStatus.SCHEDULED;
import static nl.surfnet.bod.domain.ReservationStatus.TRANSITION_STATES;
import static nl.surfnet.bod.domain.ReservationStatus.TRANSITION_STATES_AS_ARRAY;

import java.util.*;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.view.ReservationReportView;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@Service
public class ReportingService {

  @Resource
  private ReservationService reservationService;

  @Resource
  private LogEventService logEventService;

  @Resource
  private VirtualResourceGroupService virtualResourceGroupService;

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

  private void determineReservationRequestsForGroups(ReservationReportView reservationReport,
      Collection<String> adminGroups) {
    final DateTime start = reservationReport.getPeriodStart();
    final DateTime end = reservationReport.getPeriodEnd();

    // ReservationRequests
    reservationReport.setAmountRequestsCreatedSucceeded(reservationService
        .countSuccesfullReservationRequestsInAdminGroups(start, end, adminGroups));
    reservationReport.setAmountRequestsCreatedFailed(reservationService.countFailedReservationRequestsInAdminGroups(
        start, end, adminGroups));

    // No modify requests yet, init on zero
    reservationReport.setAmountRequestsModifiedSucceeded(0l);
    reservationReport.setAmountRequestsModifiedFailed(0l);

    reservationReport.setAmountRequestsCancelSucceeded(reservationService
        .countReservationsWithEndStateBetweenInAdminGroups(start, end, adminGroups, ReservationStatus.CANCELLED));

    reservationReport.setAmountRequestsCancelFailed(reservationService
        .countReservationsWithEndStateBetweenInAdminGroups(start, end, adminGroups, ReservationStatus.CANCEL_FAILED));

    // Actual Reservations by channel
    reservationReport.setAmountRequestsThroughNSI(reservationService
        .countReservationsCreatedThroughChannelNSIInAdminGroups(start, end, adminGroups)
        + reservationService.countReservationsCancelledThroughChannelNSIInAdminGroups(start, end, adminGroups));

    reservationReport.setAmountRequestsThroughGUI(reservationService
        .countReservationsCreatedThroughChannelGUIInAdminGroups(start, end, adminGroups)
        + reservationService.countReservationsCancelledThroughChannelGUInAdminGroups(start, end, adminGroups));
  }

  private void determineReservationsInAdminGroupsForProtectionType(ReservationReportView reservationReport,
      Collection<String> adminGroups) {
    final DateTime start = reservationReport.getPeriodStart();
    final DateTime end = reservationReport.getPeriodEnd();

    Set<Long> reservationIds = new HashSet<>();

    for (Long id : reservationService.findReservationIdsBeforeOrOnInAdminGroupsWithState(start, adminGroups,
        TRANSITION_STATES_AS_ARRAY)) {
      LogEvent logEvent = logEventService.findLatestStateChangeForReservationIdBeforeInAdminGroups(id, start,
          adminGroups);
      if (TRANSITION_STATES.contains(logEvent.getNewReservationStatus())) {
        reservationIds.add(id);
      }
    }
    reservationIds.addAll(reservationService.findSuccessfullReservationRequestsInAdminGroups(start, end, adminGroups));

    List<Long> reservationIdList = Lists.newArrayList(reservationIds);

    reservationReport.setAmountReservationsProtected(reservationService.countReservationsForIdsWithProtectionType(
        reservationIdList, ProtectionType.PROTECTED));

    reservationReport.setAmountReservationsUnprotected(reservationService.countReservationsForIdsWithProtectionType(
        reservationIdList, ProtectionType.UNPROTECTED));

    reservationReport.setAmountReservationsRedundant(reservationService.countReservationsForIdsWithProtectionType(
        reservationIdList, ProtectionType.REDUNDANT));
  }

  private void determineActiveRunningReservations(ReservationReportView reservationReport,
      Collection<String> adminGroups) {
    final DateTime start = reservationReport.getPeriodStart();
    final DateTime end = reservationReport.getPeriodEnd();

    List<Long> reservationIdsInPeriod = ImmutableList.copyOf(reservationService
        .findReservationIdsStartBeforeAndEndInOrAfter(start, end));

    reservationReport.setAmountRunningReservationsSucceeded(reservationService
        .countRunningReservationsInAdminGroupsSucceeded(reservationIdsInPeriod, start, end, adminGroups));

    reservationReport.setAmountRunningReservationsFailed(reservationService
        .countRunningReservationsInAdminGroupsFailed(reservationIdsInPeriod, start, end, adminGroups));

    reservationReport.setAmountRunningReservationsStillRunning(reservationService
        .countActiveReservationsBetweenWithState(reservationIdsInPeriod, start, end, RUNNING, adminGroups));

    reservationReport.setAmounRunningReservationsStillScheduled(reservationService
        .countActiveReservationsBetweenWithState(reservationIdsInPeriod, start, end, SCHEDULED, adminGroups));

    reservationReport.setAmountRunningReservationsNeverProvisioned(reservationService
        .countReservationsWithEndStateBetweenInAdminGroups(start, end, adminGroups, ReservationStatus.TIMED_OUT));
  }

}
