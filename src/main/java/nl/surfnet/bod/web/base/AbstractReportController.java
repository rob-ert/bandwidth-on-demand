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
package nl.surfnet.bod.web.base;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.event.LogEvent;
import nl.surfnet.bod.service.LogEventService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.web.view.NocReservationReport;
import nl.surfnet.bod.web.view.ReportIntervalView;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.annotations.VisibleForTesting;

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

    model.addAttribute("intervals", determineReportIntervals());
    model.addAttribute("report", determineReport(getAdminGroups()));

    return getPageUrl();
  }

  protected abstract String getPageUrl();

  protected abstract List<String> getAdminGroups();

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

  private NocReservationReport determineReport(List<String> adminGroups) {
    // TODO previous month
    final DateTime firstDayOfPreviousMonth = DateMidnight.now().withDayOfMonth(1).toDateTime();
    final DateTime lastDayOfPreviousMonth = firstDayOfPreviousMonth.dayOfMonth().withMaximumValue().toDateTime();

    NocReservationReport nocReservationReport = new NocReservationReport(firstDayOfPreviousMonth,
        lastDayOfPreviousMonth);
    determineReservationRequestsForGroups(nocReservationReport, adminGroups);
    determineReservationsForProtectionType(nocReservationReport, adminGroups);
    determineActiveRunningReservations(nocReservationReport, adminGroups);

    return nocReservationReport;
  }

  private void determineReservationRequestsForGroups(NocReservationReport nocReservationReport, List<String> adminGroups) {
    final DateTime start = nocReservationReport.getPeriodStart();
    final DateTime end = nocReservationReport.getPeriodEnd();

    // ReservationRequests
    nocReservationReport.setAmountRequestsCreatedSucceeded(reservationService.countSuccesfullReservationRequests(start,
        end));
    nocReservationReport.setAmountRequestsCreatedFailed(reservationService.countFailedReservationRequests(start, end));

    // No modify requests yet, init on zero
    nocReservationReport.setAmountRequestsModifiedSucceeded(0l);
    nocReservationReport.setAmountRequestsModifiedFailed(0l);

    nocReservationReport.setAmountRequestsCancelSucceeded(reservationService
        .countReservationsForNocWithEndStateBetween(start, end, ReservationStatus.CANCELLED));

    nocReservationReport.setAmountRequestsCancelFailed(reservationService.countReservationsForNocWithEndStateBetween(
        start, end, ReservationStatus.CANCEL_FAILED));

    // Actual Reservations by channel
    nocReservationReport.setAmountRequestsThroughGUI(reservationService
        .countReservationsForNocCreatedThroughChannelGUI(start, end));

    nocReservationReport.setAmountRequestsThroughNSI(nocReservationReport.getTotalRequests()
        - nocReservationReport.getAmountRequestsThroughGUI());
  }

  @VisibleForTesting
  void determineReservationsForProtectionType(NocReservationReport nocReservationReport, List<String> adminGroups) {
    final DateTime start = nocReservationReport.getPeriodStart();
    final DateTime end = nocReservationReport.getPeriodEnd();

    List<Long> reservationIds = new ArrayList<>();

    for (Long id : reservationService.findReservationIdsBeforeWithState(start, TRANSITION_STATES_AS_ARRAY)) {
      LogEvent logEvent = logEventService.findLatestStateChangeForReservationIdBefore(id, start);
      if (TRANSITION_STATES.contains(logEvent.getNewReservationStatus())) {
        reservationIds.add(id);
      }
    }

    reservationIds.addAll(reservationService.findSuccessfullReservationRequests(start, end));

    nocReservationReport.setAmountReservationsProtected(reservationService
        .countReservationsNocForIdsWithProtectionTypeAndCreatedBefore(reservationIds, ProtectionType.PROTECTED));

    nocReservationReport.setAmountReservationsUnprotected(reservationService
        .countReservationsNocForIdsWithProtectionTypeAndCreatedBefore(reservationIds, ProtectionType.UNPROTECTED));

    nocReservationReport.setAmountReservationsRedundant(reservationService
        .countReservationsNocForIdsWithProtectionTypeAndCreatedBefore(reservationIds, ProtectionType.REDUNDANT));
  }

  @VisibleForTesting
  void determineActiveRunningReservations(NocReservationReport nocReservationReport, List<String> adminGroups) {
    final DateTime start = nocReservationReport.getPeriodStart();
    final DateTime end = nocReservationReport.getPeriodEnd();

    nocReservationReport.setAmountRunningReservationsSucceeded(reservationService.countRunningReservationsSucceeded(
        start, end));

    nocReservationReport.setAmountRunningReservationsFailed(reservationService.countRunningReservationsFailed(start,
        end));

    nocReservationReport.setAmountRunningReservationsStillRunning(reservationService
        .countActiveReservationsBetweenWithState(start, end, RUNNING));

    nocReservationReport.setAmounRunningReservationsStillScheduled(reservationService
        .countActiveReservationsBetweenWithState(start, end, SCHEDULED));

    nocReservationReport.setAmountRunningReservationsNeverProvisioned(reservationService
        .countActiveReservationsBetweenWithState(start, end, ReservationStatus.TIMED_OUT));
  }
}
