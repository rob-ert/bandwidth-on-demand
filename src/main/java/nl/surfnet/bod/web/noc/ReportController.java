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

import javax.annotation.Resource;

import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.domain.ReservationStatus;
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

import com.google.common.base.Preconditions;

@Controller("nocReportController")
@RequestMapping(ReportController.PAGE_URL)
public class ReportController {
  public static final String PAGE_URL = "noc/report";

  @Resource(name = "bodEnvironment")
  private Environment environment;

  @Resource
  private ReservationService reservationService;

  @RequestMapping(method = RequestMethod.GET)
  public String index(Model model) {

    model.addAttribute("report", determineReport(Security.getUserDetails()));

    return PAGE_URL;
  }

  private NocReservationReport determineReport(RichUserDetails userDetails) {
    Preconditions.checkArgument(userDetails.isSelectedNocRole());

    final DateTime firstDayOfPreviousMonth = DateMidnight.now().minusMonths(1).withDayOfMonth(1).toDateTime();
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
        .countReservationsForNocWhichHadStateBetween(ReservationStatus.SCHEDULED, start, end));
    nocReservationReport.setAmountRequestsCreatedFailed(reservationService.countReservationsForNocWithEndStateBetween(
        ReservationStatus.NOT_ACCEPTED, start, end));

    // No modify requests yet, init on zero

    // Cancel requests will never fail (init on zero), only succeed
    nocReservationReport.setAmountRequestsCancelSucceeded(reservationService
        .countReservationsForNocWithEndStateBetween(ReservationStatus.CANCELLED, start, end));

    // Actual Reservations by channel
    nocReservationReport.setAmountRequestsThroughGUI(reservationService
        .countReservationsForNocCreatedThroughChannelGUI(start, end));

    nocReservationReport.setAmountRequestsThroughNSI(reservationService
        .countReservationsForNocCreatedThroughChannelNSI(start, end));

  }

  private void determineReservationsForProtectionType(NocReservationReport nocReservationReport) {
    final DateTime start = nocReservationReport.getPeriodStart();
    final DateTime end = nocReservationReport.getPeriodEnd();

    nocReservationReport.setAmountReservationsProtected(reservationService
        .countReservationsForNocWithProtectionTypeWithNonFinalStateOnStartAndSuccesfullyCreatedBetween(
            ProtectionType.PROTECTED, start, end));
    nocReservationReport.setAmountReservationsUnprotected(reservationService
        .countReservationsForNocWithProtectionTypeWithNonFinalStateOnStartAndSuccesfullyCreatedBetween(
            ProtectionType.UNPROTECTED, start, end));
    nocReservationReport.setAmountReservationsRedundant(reservationService
        .countReservationsForNocWithProtectionTypeWithNonFinalStateOnStartAndSuccesfullyCreatedBetween(
            ProtectionType.REDUNDANT, start, end));
  }

  private void determineActiveRunningReservations(NocReservationReport nocReservationReport) {
    final DateTime start = nocReservationReport.getPeriodStart();
    final DateTime end = nocReservationReport.getPeriodEnd();

    nocReservationReport.setAmountRunningReservationsSucceeded(reservationService
        .countRunningActiveRunningReservationForNocWithStateBetween(ReservationStatus.SUCCEEDED, start, end));

    nocReservationReport.setAmountRunningReservationsFailed(reservationService
        .countRunningActiveRunningReservationForNocWithStateBetween(ReservationStatus.FAILED, start, end));

    nocReservationReport.setAmountRunningReservationsNeverProvisioned(reservationService
        .countRunningActiveRunningReservationForNocWithStateBetween(ReservationStatus.RUNNING, start, end));

    nocReservationReport.setAmountRunningReservationsFailed(reservationService
        .countRunningActiveRunningReservationForNocWithStateBetween(ReservationStatus.TIMED_OUT, start, end));
  }

}
