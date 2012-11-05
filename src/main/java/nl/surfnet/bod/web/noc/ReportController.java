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

    final DateTime periodStart = DateMidnight.now().minusMonths(1).withDayOfMonth(1).toDateTime();
    final DateTime periodEnd = periodStart.dayOfMonth().withMaximumValue().toDateTime();

    NocReservationReport nocReservationReport = new NocReservationReport(periodStart, periodEnd);

    // ReservationRequests
    nocReservationReport.setAmountFailedReservationRequests(reservationService.countReservationsWithEndStateBetween(
        ReservationStatus.NOT_ACCEPTED, periodStart, periodEnd));
    nocReservationReport.setAmountSucceededReservationRequests(reservationService
        .countReservationsWhichOnceHadStateBetween(periodStart, periodEnd, ReservationStatus.REQUEST_SUCCEEDED));

    // Actual Reservations by protection type
    nocReservationReport.setAmountProtectedReservations(reservationService.countReservationsWithProtectionTypeBetween(
        ProtectionType.PROTECTED, periodStart, periodEnd));
    nocReservationReport.setAmountUnprotectedReservations(reservationService
        .countReservationsWithProtectionTypeBetween(ProtectionType.UNPROTECTED, periodStart, periodEnd));
    nocReservationReport.setAmountRedundantReservations(reservationService.countReservationsWithProtectionTypeBetween(
        ProtectionType.REDUNDANT, periodStart, periodEnd));

    // Actual Reservations by channel
   //Only GUI reservation Requests get this state
   nocReservationReport.setAmountGUIReservations(reservationService.countReservationsCreatedThroughChannelGUI(periodStart, periodEnd));
   nocReservationReport.setAmountNSIReservations(nocReservationReport.getTotalReservations()-nocReservationReport.getAmountGUIReservations());

    // Actual Reservations by status
    nocReservationReport.setAmountSucceededReservations(reservationService.countReservationsWithEndStateBetween(
        ReservationStatus.SUCCEEDED, periodStart, periodEnd));

    nocReservationReport.setAmountCancelledReservations(reservationService.countReservationsWithEndStateBetween(
        ReservationStatus.CANCELLED, periodStart, periodEnd));

    nocReservationReport.setAmountFailedReservations(reservationService.countReservationsWithEndStateBetween(
        ReservationStatus.FAILED, periodStart, periodEnd));

    return nocReservationReport;
  }
}
