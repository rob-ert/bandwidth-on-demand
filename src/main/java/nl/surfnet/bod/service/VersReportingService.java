/**
 * Copyright (c) 2012, SURFnet BV
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

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.vers.SURFnetErStub;
import nl.surfnet.bod.web.view.ReservationReportView;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import surfnet_er.ErInsertReportDocument;
import surfnet_er.ErInsertReportDocument.ErInsertReport;
import surfnet_er.InsertReportInput;

import com.google.common.base.Optional;

@Service
public class VersReportingService {

  @Value("${vers.url}")
  private String serviceURL;// = "http://localhost:1234";

  @Value("${vers.user}")
  private String versUserName;

  @Value("${vers.password}")
  private String versUserPassword;

  @Resource
  private ReportingService reportingService;

  @Resource
  private PhysicalResourceGroupService physicalResourceGroupService;

  @Resource
  private Map<String, Map<String, String>> reportToVersMap;

  private SURFnetErStub surfNetErStub;

  private final String firstDayOfTheMonthCronExpression = "0 0 0 1 * ?";

  private final DateTimeFormatter versFormatter = DateTimeFormat.forPattern("yyyy-MM");

  @PostConstruct
  void init() throws IOException {
    surfNetErStub = new SURFnetErStub(serviceURL);
  }

  @Scheduled(cron = firstDayOfTheMonthCronExpression)
  public void sendInternalReport() throws Exception {

    final Map<Optional<String>, ReservationReportView> reportViews = getAllReservationReportViews();

    for (final Entry<Optional<String>, ReservationReportView> reservationReportViewEntry : reportViews.entrySet()) {

      for (final Entry<String, Map<String, String>> entry : reportToVersMap.entrySet()) {
        String text = entry.getKey();
        long valuePos, valueNeg;

        switch (entry.getKey()) {

        case "amountReservationsProtected":
          valuePos = reservationReportViewEntry.getValue().getAmountReservationsProtected();
          valueNeg = reservationReportViewEntry.getValue().getAmountReservationsUnprotected();
          if (entry.getValue().get("TRUE") != null) {
            text = entry.getValue().get("TRUE");
            surfNetErStub.er_InsertReport(getVersRequest("Reservations: Protection Types", Long.toString(valuePos),
                reservationReportViewEntry.getValue().getPeriodStart(), reservationReportViewEntry.getKey(), text));
          }
          if (entry.getValue().get("FALSE") != null) {
            text = entry.getValue().get("FALSE");
            surfNetErStub.er_InsertReport(getVersRequest("Reservations: Protection Types", Long.toString(valueNeg),
                reservationReportViewEntry.getValue().getPeriodStart(), reservationReportViewEntry.getKey(), text));
          }
          text = "Redundant";
          surfNetErStub.er_InsertReport(getVersRequest("Reservations: Protection Types",
              Long.toString(reservationReportViewEntry.getValue().getAmountReservationsRedundant()),
              reservationReportViewEntry.getValue().getPeriodStart(), reservationReportViewEntry.getKey(), text));
          break;

        case "amountRunningReservationsSucceeded":
          valuePos = reservationReportViewEntry.getValue().getAmountRequestsCreatedSucceeded();
          valueNeg = reservationReportViewEntry.getValue().getAmountRequestsCreatedFailed();
          if (entry.getValue().get("TRUE") != null) {
            text = entry.getValue().get("TRUE");
            surfNetErStub.er_InsertReport(getVersRequest("Reservations created", Long.toString(valuePos),
                reservationReportViewEntry.getValue().getPeriodStart(), reservationReportViewEntry.getKey(), text));
          }
          if (entry.getValue().get("FALSE") != null) {
            text = entry.getValue().get("FALSE");
            surfNetErStub.er_InsertReport(getVersRequest("Reservations created", Long.toString(valueNeg),
                reservationReportViewEntry.getValue().getPeriodStart(), reservationReportViewEntry.getKey(), text));
          }
          break;

        case "amountRunningReservationsStillRunning":
          if (entry.getValue().get("TRUE") != null) {
            text = entry.getValue().get("TRUE");
            surfNetErStub.er_InsertReport(getVersRequest("Active Reservations: Running",
                Long.toString(reservationReportViewEntry.getValue().getAmountRunningReservationsStillRunning()),
                reservationReportViewEntry.getValue().getPeriodStart(), reservationReportViewEntry.getKey(), text));

            surfNetErStub.er_InsertReport(getVersRequest("Active Reservations: Running",
                Long.toString(reservationReportViewEntry.getValue().getAmountRunningReservationsSucceeded()),
                reservationReportViewEntry.getValue().getPeriodStart(), reservationReportViewEntry.getKey(),
                "Execution succeeded"));

            surfNetErStub.er_InsertReport(getVersRequest("Active Reservations: Running",
                Long.toString(reservationReportViewEntry.getValue().getAmountRunningReservationsFailed()),
                reservationReportViewEntry.getValue().getPeriodStart(), reservationReportViewEntry.getKey(),
                "Execution failed"));

            surfNetErStub.er_InsertReport(getVersRequest("Active Reservations: Running",
                Long.toString(reservationReportViewEntry.getValue().getAmountRunningReservationsSucceeded()),
                reservationReportViewEntry.getValue().getPeriodStart(), reservationReportViewEntry.getKey(),
                "Execution succeeded"));

            surfNetErStub.er_InsertReport(getVersRequest("Active Reservations: Running",
                Long.toString(reservationReportViewEntry.getValue().getAmountRunningReservationsStillScheduled()),
                reservationReportViewEntry.getValue().getPeriodStart(), reservationReportViewEntry.getKey(),
                "Scheduled"));
          }
          break;

        case "amountRequestsThroughGUI":
          valuePos = reservationReportViewEntry.getValue().getAmountRequestsThroughNSI();
          valueNeg = reservationReportViewEntry.getValue().getAmountRequestsThroughGUI();
          if (entry.getValue().get("TRUE") != null) {
            text = entry.getValue().get("TRUE");
            surfNetErStub.er_InsertReport(getVersRequest("Reservations through", Long.toString(valuePos),
                reservationReportViewEntry.getValue().getPeriodStart(), reservationReportViewEntry.getKey(), text));
          }
          if (entry.getValue().get("FALSE") != null) {
            text = entry.getValue().get("FALSE");
            surfNetErStub.er_InsertReport(getVersRequest("Reservations through", Long.toString(valueNeg),
                reservationReportViewEntry.getValue().getPeriodStart(), reservationReportViewEntry.getKey(), text));
          }
          break;

        case "amountRunningReservationsNeverProvisioned":
          valuePos = reservationReportViewEntry.getValue().getAmountRunningReservationsNeverProvisioned();
          if (entry.getValue().get("TRUE") != null) {
            text = entry.getValue().get("TRUE");
            surfNetErStub.er_InsertReport(getVersRequest("Never provisioned", Long.toString(valuePos),
                reservationReportViewEntry.getValue().getPeriodStart(), reservationReportViewEntry.getKey(), text));
          }
          break;

        case "amountRequestsModifiedSucceeded":
          valuePos = reservationReportViewEntry.getValue().getAmountRequestsModifiedSucceeded();
          valueNeg = reservationReportViewEntry.getValue().getAmountRequestsModifiedFailed();
          if (entry.getValue().get("TRUE") != null) {
            text = entry.getValue().get("TRUE");
            surfNetErStub.er_InsertReport(getVersRequest("Reservation modified", Long.toString(valuePos),
                reservationReportViewEntry.getValue().getPeriodStart(), reservationReportViewEntry.getKey(), text));
          }
          if (entry.getValue().get("FALSE") != null) {
            text = entry.getValue().get("FALSE");
            surfNetErStub.er_InsertReport(getVersRequest("Reservation modified", Long.toString(valueNeg),
                reservationReportViewEntry.getValue().getPeriodStart(), reservationReportViewEntry.getKey(), text));
          }
          break;

        case "amountRequestsCancelSucceeded":
          valuePos = reservationReportViewEntry.getValue().getAmountRequestsCancelSucceeded();
          valueNeg = reservationReportViewEntry.getValue().getAmountRequestsCancelFailed();
          if (entry.getValue().get("TRUE") != null) {
            text = entry.getValue().get("TRUE");
            surfNetErStub.er_InsertReport(getVersRequest("Reservation Cancelled", Long.toString(valuePos),
                reservationReportViewEntry.getValue().getPeriodStart(), reservationReportViewEntry.getKey(), text));
          }
          if (entry.getValue().get("FALSE") != null) {
            text = entry.getValue().get("FALSE");
            surfNetErStub.er_InsertReport(getVersRequest("Reservation Cancelled", Long.toString(valueNeg),
                reservationReportViewEntry.getValue().getPeriodStart(), reservationReportViewEntry.getKey(), text));
          }
          break;

        default:
          surfNetErStub.er_InsertReport(getVersRequest(entry.getKey(), "-TBD-", reservationReportViewEntry.getValue()
              .getPeriodStart(), reservationReportViewEntry.getKey(), "-TBD-"));
          break;
        }
      }
    }
  }

  private Map<Optional<String>, ReservationReportView> getAllReservationReportViews() {
    final Map<Optional<String>, ReservationReportView> reportViews = new HashMap<>();
    final VersReportPeriod versReportPeriod = new VersReportPeriod();

    // using absent for noc report will hide report from non surfnet eyes
    reportViews.put(Optional.<String> absent(), reportingService.determineReportForNoc(versReportPeriod.getInterval()));

    final Collection<PhysicalResourceGroup> prgWithPorts = physicalResourceGroupService.findAllWithPorts();
    for (PhysicalResourceGroup physicalResourceGroup : prgWithPorts) {
      reportViews.put(
          Optional.of(physicalResourceGroup.getInstitute().getShortName()),
          reportingService.determineReportForAdmin(versReportPeriod.getInterval(),
              BodRole.createManager(physicalResourceGroup)));
    }
//    System.out.println(reportViews);
    return reportViews;
  }

  private ErInsertReportDocument getVersRequest(final String type, final String value, DateTime start,
      final Optional<String> instituteShortName, final String instance) {
    final ErInsertReportDocument versRequest = ErInsertReportDocument.Factory.newInstance();
    final InsertReportInput insertReportInput = InsertReportInput.Factory.newInstance();
    insertReportInput.setType(type);
    insertReportInput.setInstance(instance);
    insertReportInput.setNormComp("=");
    insertReportInput.setNormValue(value);
    insertReportInput.setDepartmentList("NWD");
    insertReportInput.setIsKPI(true);
    insertReportInput.setValue(value);
    final String date = versFormatter.print(new DateTime().withYear(2001).withDayOfYear(1).plusMonths(10).withHourOfDay(0)
        .withMinuteOfHour(0));
    insertReportInput.setPeriod(date);

    if (instituteShortName.isPresent()) {
//      System.out.println("Using type: " + type + " Organisation: " + instituteShortName.get() + " value: " + value
//          + " for instance: " + instance + " and hidden is false");
      insertReportInput.setOrganisation(instituteShortName.get());
      insertReportInput.setIsHidden(false);
    }
    else {
      insertReportInput.setIsHidden(true);
    }
    versRequest.setErInsertReport(getErInsertReport(insertReportInput));
    return versRequest;
  }

  private ErInsertReport getErInsertReport(final InsertReportInput reportData) {
    final ErInsertReport messageBody = ErInsertReport.Factory.newInstance();
    messageBody.setUsername(versUserName);
    messageBody.setPassword(versUserPassword);
    messageBody.setParameters(reportData);
    return messageBody;
  }

  public class VersReportPeriod {
    private final DateTime start = LocalDateTime.now().minusMonths(1).withHourOfDay(0).withMinuteOfHour(0)
        .withSecondOfMinute(0).withDayOfWeek(1).toDateTime();
    private final DateTime end = LocalDateTime.now().toDateTime().minusDays(1);
    private final Interval interval = new Interval(start, end);

    public final Interval getInterval() {
      System.out.println(interval.getStart() + " " + interval.getEnd());
      return interval;
    }

  }

}
