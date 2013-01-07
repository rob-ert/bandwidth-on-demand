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

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

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
  private InstituteIddService instituteIddService;

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

    final VersReportPeriod versReportPeriod = new VersReportPeriod();
    final ReservationReportView nocReports = reportingService.determineReportForNoc(versReportPeriod.getInterval());

    for (final Entry<String, Map<String, String>> entry : reportToVersMap.entrySet()) {
      String text = entry.getKey();
      long valuePos, valueNeg;

      switch (entry.getKey()) {

      // TODO Will come up with something to reduce this mess, hopefully.
      case "amountReservationsProtected":
        valuePos = nocReports.getAmountReservationsProtected();
        valueNeg = nocReports.getAmountReservationsUnprotected();
        if (entry.getValue().get("TRUE") != null) {
          text = entry.getValue().get("TRUE");
          surfNetErStub.er_InsertReport(getVersRequest(
              "Reservations: Protection Types", Long.toString(valuePos), nocReports.getPeriodStart(), Optional.<String> absent(),
              text));
        }
        if (entry.getValue().get("FALSE") != null) {
          text = entry.getValue().get("FALSE");
          surfNetErStub.er_InsertReport(getVersRequest("Reservations: Protection Types", Long.toString(valueNeg),
              nocReports.getPeriodStart(), Optional.<String> absent(), text));
        }
        text = "Redundant";
        surfNetErStub.er_InsertReport(getVersRequest("Reservations: Protection Types",
            Long.toString(nocReports.getAmountReservationsRedundant()), nocReports.getPeriodStart(),
            Optional.<String> absent(), text));
        break;

      case "amountRunningReservationsSucceeded":
        valuePos = nocReports.getAmountRunningReservationsSucceeded();
        valueNeg = nocReports.getAmountRunningReservationsFailed();
        if (entry.getValue().get("TRUE") != null) {
          text = entry.getValue().get("TRUE");
          surfNetErStub.er_InsertReport(getVersRequest("Reservations created", Long.toString(valuePos),
              nocReports.getPeriodStart(), Optional.<String> absent(), text));
        }
        if (entry.getValue().get("FALSE") != null) {
          text = entry.getValue().get("FALSE");
          surfNetErStub.er_InsertReport(getVersRequest("Reservations created", Long.toString(valueNeg),
              nocReports.getPeriodStart(), Optional.<String> absent(), text));
        }
        break;

      case "amountRunningReservationsStillRunning":
        if (entry.getValue().get("TRUE") != null) {
          text = entry.getValue().get("TRUE");
          surfNetErStub.er_InsertReport(getVersRequest("Active Reservations: Running",
              Long.toString(nocReports.getAmountRunningReservationsStillRunning()), nocReports.getPeriodStart(),
              Optional.<String> absent(), text));

          surfNetErStub.er_InsertReport(getVersRequest("Active Reservations: Running",
              Long.toString(nocReports.getAmountRunningReservationsSucceeded()), nocReports.getPeriodStart(),
              Optional.<String> absent(), "Execution succeeded"));

          surfNetErStub.er_InsertReport(getVersRequest("Active Reservations: Running",
              Long.toString(nocReports.getAmountRunningReservationsFailed()), nocReports.getPeriodStart(),
              Optional.<String> absent(), "Execution failed"));

          surfNetErStub.er_InsertReport(getVersRequest("Active Reservations: Running",
              Long.toString(nocReports.getAmountRunningReservationsSucceeded()), nocReports.getPeriodStart(),
              Optional.<String> absent(), "Execution succeeded"));

          surfNetErStub.er_InsertReport(getVersRequest("Active Reservations: Running",
              Long.toString(nocReports.getAmountRunningReservationsStillScheduled()), nocReports.getPeriodStart(),
              Optional.<String> absent(), "Scheduled"));
        }
        break;

      case "amountRequestsThroughGUI":
        valuePos = nocReports.getAmountRequestsThroughNSI();
        valueNeg = nocReports.getAmountRequestsThroughGUI();
        if (entry.getValue().get("TRUE") != null) {
          text = entry.getValue().get("TRUE");
          surfNetErStub.er_InsertReport(getVersRequest("Reservations through", Long.toString(valuePos),
              nocReports.getPeriodStart(), Optional.<String> absent(), text));
        }
        if (entry.getValue().get("FALSE") != null) {
          text = entry.getValue().get("FALSE");
          surfNetErStub.er_InsertReport(getVersRequest("Reservations through", Long.toString(valueNeg),
              nocReports.getPeriodStart(), Optional.<String> absent(), text));
        }
        break;

      case "amountRunningReservationsNeverProvisioned":
        valuePos = nocReports.getAmountRunningReservationsNeverProvisioned();
        if (entry.getValue().get("TRUE") != null) {
          text = entry.getValue().get("TRUE");
          surfNetErStub.er_InsertReport(getVersRequest("Never provisioned", Long.toString(valuePos),
              nocReports.getPeriodStart(), Optional.<String> absent(), text));
        }
        break;

      case "amountRequestsModifiedSucceeded":
        valuePos = nocReports.getAmountRequestsModifiedSucceeded();
        valueNeg = nocReports.getAmountRequestsModifiedFailed();
        if (entry.getValue().get("TRUE") != null) {
          text = entry.getValue().get("TRUE");
          surfNetErStub.er_InsertReport(getVersRequest("Reservation modified", Long.toString(valuePos),
              nocReports.getPeriodStart(), Optional.<String> absent(), text));
        }
        if (entry.getValue().get("FALSE") != null) {
          text = entry.getValue().get("FALSE");
          surfNetErStub.er_InsertReport(getVersRequest("Reservation modified", Long.toString(valueNeg),
              nocReports.getPeriodStart(), Optional.<String> absent(), text));
        }
        break;

      case "amountRequestsCancelSucceeded":
        valuePos = nocReports.getAmountRequestsCancelSucceeded();
        valueNeg = nocReports.getAmountRequestsCancelFailed();
        if (entry.getValue().get("TRUE") != null) {
          text = entry.getValue().get("TRUE");
          surfNetErStub.er_InsertReport(getVersRequest("Reservation Cancelled", Long.toString(valuePos),
              nocReports.getPeriodStart(), Optional.<String> absent(), text));
        }
        if (entry.getValue().get("FALSE") != null) {
          text = entry.getValue().get("FALSE");
          surfNetErStub.er_InsertReport(getVersRequest("Reservation Cancelled", Long.toString(valueNeg),
              nocReports.getPeriodStart(), Optional.<String> absent(), text));
        }
        break;

      default:
        surfNetErStub.er_InsertReport(getVersRequest(entry.getKey(), "TBD", nocReports.getPeriodStart(),
            Optional.<String> absent(), "TBD"));
        break;

      }
    }
  }

  private ErInsertReportDocument getVersRequest(final String type, final String value, DateTime start,
      Optional<String> instituteShortName, String instance) {
    final ErInsertReportDocument versRequest = ErInsertReportDocument.Factory.newInstance();
    final InsertReportInput insertReportInput = InsertReportInput.Factory.newInstance();
    insertReportInput.setType(type);
    insertReportInput.setInstance(instance);
    insertReportInput.setNormComp("=");
    insertReportInput.setNormValue(value);
    insertReportInput.setDepartmentList("NWD");
    insertReportInput.setIsKPI(true);
    insertReportInput.setValue(value);
    final String date = versFormatter.print(DateTime.now().minusMonths(1));
//    final String date = versFormatter.print(DateTime.now().minusYears(2).minusMonths(1));
    insertReportInput.setPeriod(date);

    if (instituteShortName.isPresent()) {
      insertReportInput.setOrganisation(instituteShortName.get());
      insertReportInput.setIsHidden(false);
    }
    else {
      insertReportInput.setIsHidden(true);
    }
    versRequest.setErInsertReport(getErInsertReport(insertReportInput));

    // System.out.println(ReflectionToStringBuilder.toString(insertReportInput,
    // ToStringStyle.MULTI_LINE_STYLE));

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
    private final DateTime start = LocalDateTime.now().minusMonths(1).toDateTime();
    private final DateTime end = LocalDateTime.now().toDateTime().plusDays(10);
    private final Interval interval = new Interval(start, end);

    public final Interval getInterval() {
      // System.out.println(interval.getStart() +" "+ interval.getEnd());
      return interval;
    }

  }

  public static void main(String args[]) throws Exception {
    // final ReservationReportView reservationReportViewNoc = new
    // ReservationReportView(DateTime.now(), DateTime.now()
    // .plusHours(1));
    for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(ReservationReportView.class)
        .getPropertyDescriptors()) {
      if (propertyDescriptor.getPropertyType() == java.lang.Class.class) {

      }
      else {
        System.out.println(propertyDescriptor);
      }
    }
    System.out.println();

  }

}
