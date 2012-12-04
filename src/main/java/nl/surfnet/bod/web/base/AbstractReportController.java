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
package nl.surfnet.bod.web.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.service.LogEventService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.web.view.ReportIntervalView;
import nl.surfnet.bod.web.view.ReservationReportView;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import static nl.surfnet.bod.domain.ReservationStatus.RUNNING;
import static nl.surfnet.bod.domain.ReservationStatus.SCHEDULED;
import static nl.surfnet.bod.domain.ReservationStatus.TRANSITION_STATES;
import static nl.surfnet.bod.domain.ReservationStatus.TRANSITION_STATES_AS_ARRAY;

public abstract class AbstractReportController {
  protected static final int AMOUNT_OF_REPORT_PERIODS = 8;

  @Resource
  private ReservationService reservationService;

  @Resource
  private LogEventService logEventService;

  @RequestMapping(method = RequestMethod.GET)
  public String index(Model model) {

    return index(null, model);
  }

  @RequestMapping(value = "/{selectedIntervalId}", method = RequestMethod.GET)
  public String index(@PathVariable final Integer selectedIntervalId, Model model) {

    List<ReportIntervalView> intervals = determineReportIntervals();
    model.addAttribute("intervalList", intervals);
    model.addAttribute("baseReportIntervalUrl", getPageUrl());

    ReportIntervalView selectedInterval = intervals.get(0);
    if (selectedIntervalId != null) {
      selectedInterval = Iterables.find(intervals, new Predicate<ReportIntervalView>() {
        @Override
        public boolean apply(ReportIntervalView interval) {
          return selectedIntervalId.equals(interval.getId());
        }
      });
    }
    model.addAttribute("selectedInterval", selectedInterval);
    model.addAttribute("report", determineReport(selectedInterval, getAdminGroups()));
    return getPageUrl();
  }

  protected abstract String getPageUrl();

  protected abstract Collection<String> getAdminGroups();

  @VisibleForTesting
  List<ReportIntervalView> determineReportIntervals() {
    final DateTimeFormatter labelFormatter = DateTimeFormat.forPattern("yyyy MMM");
    final List<ReportIntervalView> reportIntervals = new ArrayList<>();

    String label;
    Interval reportInterval;
    DateTime firstDayOfInterval;
    DateTime lastDayOfInterval;

    for (int i = 0; i < AMOUNT_OF_REPORT_PERIODS; i++) {
      firstDayOfInterval = DateMidnight.now().withDayOfMonth(1).minusMonths(i).toDateTime();
      lastDayOfInterval = firstDayOfInterval.dayOfMonth().withMaximumValue().toDateTime();

      label = labelFormatter.print(firstDayOfInterval);

      // Correct to today
      if (lastDayOfInterval.isAfterNow()) {
        lastDayOfInterval = firstDayOfInterval.withDayOfMonth(DateTime.now().getDayOfMonth());
        label += " - now";
      }
      reportInterval = new Interval(firstDayOfInterval, lastDayOfInterval);
      reportIntervals.add(new ReportIntervalView(reportInterval, label));
    }

    return reportIntervals;
  }

  private ReservationReportView determineReport(ReportIntervalView selectedInterval, Collection<String> adminGroups) {
    ReservationReportView reservationReport = new ReservationReportView(selectedInterval.getInterval().getStart(),
        selectedInterval.getInterval().getEnd());

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
        .countReservationsWithEndStateBetweenInAdminGroups(start, end, adminGroups,
            ReservationStatus.CANCEL_FAILED));

    // Actual Reservations by channel
    reservationReport.setAmountRequestsThroughGUI(reservationService
        .countReservationsCreatedThroughChannelGUIInAdminGroups(start, end, adminGroups));

    reservationReport.setAmountRequestsThroughNSI(reservationReport.getTotalRequests()
        - reservationReport.getAmountRequestsThroughGUI());
  }

  @VisibleForTesting
  void determineReservationsInAdminGroupsForProtectionType(ReservationReportView reservationReport,
      Collection<String> adminGroups) {
    final DateTime start = reservationReport.getPeriodStart();
    final DateTime end = reservationReport.getPeriodEnd();

    List<Long> reservationIds = new ArrayList<>();

    for (Long id : reservationService.findReservationIdsBeforeInAdminGroupsWithState(start, adminGroups,
        TRANSITION_STATES_AS_ARRAY)) {
      LogEvent logEvent = logEventService.findLatestStateChangeForReservationIdBeforeInAdminGroups(id, start,
          adminGroups);
      if (TRANSITION_STATES.contains(logEvent.getNewReservationStatus())) {
        reservationIds.add(id);
      }
    }

    reservationIds.addAll(reservationService.findSuccessfullReservationRequestsInAdminGroups(start, end, adminGroups));

    reservationReport.setAmountReservationsProtected(reservationService
        .countReservationsForIdsWithProtectionTypeAndCreatedBefore(reservationIds, ProtectionType.PROTECTED));

    reservationReport.setAmountReservationsUnprotected(reservationService
        .countReservationsForIdsWithProtectionTypeAndCreatedBefore(reservationIds, ProtectionType.UNPROTECTED));

    reservationReport.setAmountReservationsRedundant(reservationService
        .countReservationsForIdsWithProtectionTypeAndCreatedBefore(reservationIds, ProtectionType.REDUNDANT));
  }

  @VisibleForTesting
  void determineActiveRunningReservations(ReservationReportView reservationReport, Collection<String> adminGroups) {
    final DateTime start = reservationReport.getPeriodStart();
    final DateTime end = reservationReport.getPeriodEnd();

    reservationReport.setAmountRunningReservationsSucceeded(reservationService
        .countRunningReservationsInAdminGroupsSucceeded(start, end, adminGroups));

    reservationReport.setAmountRunningReservationsFailed(reservationService
        .countRunningReservationsInAdminGroupsFailed(start, end, adminGroups));

    reservationReport.setAmountRunningReservationsStillRunning(reservationService
        .countActiveReservationsBetweenWithState(start, end, RUNNING, adminGroups));

    reservationReport.setAmounRunningReservationsStillScheduled(reservationService
        .countActiveReservationsBetweenWithState(start, end, SCHEDULED, adminGroups));

    reservationReport.setAmountRunningReservationsNeverProvisioned(reservationService
        .countActiveReservationsBetweenWithState(start, end, ReservationStatus.TIMED_OUT, adminGroups));
  }
}
