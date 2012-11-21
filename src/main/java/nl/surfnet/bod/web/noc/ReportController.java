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
package nl.surfnet.bod.web.noc;

import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.service.LogEventService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.NocReservationReport;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.annotations.VisibleForTesting;

import static nl.surfnet.bod.domain.ReservationStatus.RUNNING;

@Controller("nocReportController")
@RequestMapping(ReportController.PAGE_URL)
public class ReportController {
  public static final String PAGE_URL = "noc/report";

  @Resource(name = "bodEnvironment")
  private Environment environment;

  @Resource
  private ReservationService reservationService;

  @Resource
  private LogEventService logEventService;

  @RequestMapping(method = RequestMethod.GET)
  public String index(Model model) {

    model.addAttribute("report", determineReport(Security.getUserDetails()));

    return PAGE_URL;
  }

  private NocReservationReport determineReport(RichUserDetails userDetails) {
    // TODO previous month
    final DateTime firstDayOfPreviousMonth = DateMidnight.now().withDayOfMonth(1).toDateTime();
    final DateTime lastDayOfPreviousMonth = firstDayOfPreviousMonth.dayOfMonth().withMaximumValue().toDateTime();

    NocReservationReport nocReservationReport = new NocReservationReport(firstDayOfPreviousMonth,
        lastDayOfPreviousMonth);
    determineReservationRequests(nocReservationReport);
    determineReservationsForProtectionType(nocReservationReport);
    determineActiveRunningReservations(nocReservationReport);

    return nocReservationReport;
  }

  private void determineReservationRequests(NocReservationReport nocReservationReport) {
    final DateTime start = nocReservationReport.getPeriodStart();
    final DateTime end = nocReservationReport.getPeriodEnd();

    // ReservationRequests
    nocReservationReport.setAmountRequestsCreatedSucceeded(reservationService
        .countReservationsForNocWhichHadStateBetween(start, end, ReservationStatus.SUCCESSFULLY_CREATED));

    nocReservationReport.setAmountRequestsCreatedFailed(reservationService.countReservationsForNocWithEndStateBetween(
        start, end, ReservationStatus.NOT_ACCEPTED));
    // TODO should also check FAILED

    // No modify requests yet, init on zero

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
  void determineReservationsForProtectionType(NocReservationReport nocReservationReport) {
    final DateTime start = nocReservationReport.getPeriodStart();

    List<Long> reservationIds = reservationService.findReservationIdsBeforeWithLatestState(start,
        ReservationStatus.TRANSITION_STATES_AS_ARRAY);

    nocReservationReport.setAmountReservationsProtected(reservationService
        .countReservationsNocForIdsWithProtectionTypeAndCreatedBefore(reservationIds, ProtectionType.PROTECTED));

    nocReservationReport.setAmountReservationsUnprotected(reservationService
        .countReservationsNocForIdsWithProtectionTypeAndCreatedBefore(reservationIds, ProtectionType.UNPROTECTED));

    nocReservationReport.setAmountReservationsRedundant(reservationService
        .countReservationsNocForIdsWithProtectionTypeAndCreatedBefore(reservationIds, ProtectionType.REDUNDANT));
  }

  @VisibleForTesting
  void determineActiveRunningReservations(NocReservationReport nocReservationReport) {
    final DateTime start = nocReservationReport.getPeriodStart();
    final DateTime end = nocReservationReport.getPeriodEnd();

    nocReservationReport.setAmountRunningReservationsSucceeded(reservationService.countRunningReservationsSucceeded(
        start, end));

    nocReservationReport.setAmountRunningReservationsNeverProvisioned(reservationService
        .countActiveReservationsBetweenWithStatusIn(start, end, ReservationStatus.TIMED_OUT));

    nocReservationReport.setAmounRunningReservationsStillRunning(reservationService
        .countActiveReservationsBetweenWithStatusIn(start, end, RUNNING));
  }
}
