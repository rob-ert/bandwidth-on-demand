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

import java.rmi.RemoteException;
import java.util.*;
import java.util.Map.Entry;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.vers.SURFnetErStub;
import nl.surfnet.bod.web.view.ReservationReportView;

import org.joda.time.YearMonth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import surfnet_er.ErInsertReportDocument;
import surfnet_er.ErInsertReportDocument.ErInsertReport;
import surfnet_er.ErInsertReportResponseDocument.ErInsertReportResponse;
import surfnet_er.InsertReportInput;

import com.google.common.base.Optional;

@Service
public class VersReportingService {

  private static final String DEPARTMENT_NETWERK_DIENSTEN = "NWD";
  // Every 1st of the month at 1:00am
  private static final String FIRST_DAY_OF_THE_MONTH_CRON_EXPRESSION = "0 0 1 1 1/1 ?";

  @Value("${vers.url}")
  private String serviceURL;

  @Value("${vers.user}")
  private String versUserName;

  @Value("${vers.password}")
  private String versUserPassword;

  @Resource
  private ReportingService reportingService;

  @Resource
  private PhysicalResourceGroupService physicalResourceGroupService;

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Scheduled(cron = FIRST_DAY_OF_THE_MONTH_CRON_EXPRESSION)
  public void sendInternalReport() throws Exception {
    YearMonth previousMonth = YearMonth.now().minusMonths(1);

    sendReports(previousMonth);
  }

  public void sendReports(YearMonth period) {
    log.info(String.format("Sending reports to VERS for '%s'", period.toString("yyyy-MM")));

    Map<Optional<String>, ReservationReportView> reportViews = getAllReservationReportViews(period);

    for (Entry<Optional<String>, ReservationReportView> reservationReportViewEntry : reportViews.entrySet()) {

      Optional<String> institute = reservationReportViewEntry.getKey();
      ReservationReportView reportView = reservationReportViewEntry.getValue();

      Collection<ErInsertReportDocument> reports = createAllReports(period, institute, reportView);

      log.info(String.format("Sending %d reports for institute '%s'", reports.size(), institute.or("Null/Not set")));

      submitReports(reports);
    }
  }

  Collection<ErInsertReportDocument> createAllReports(YearMonth period, Optional<String> institute, ReservationReportView reportView) {
    List<ErInsertReportDocument> reports = new ArrayList<>();

    reports.addAll(createAmountReservationsProtectedPki(period, institute, reportView));
    reports.addAll(createRunningReservationsSucceededPki(period, institute, reportView));
    reports.addAll(createAmountRunningReservationsStillRunningPki(period, institute, reportView));
    reports.addAll(createAmountRequestsThroughGUIPki(period, institute, reportView));
    reports.addAll(createAmountRunningReservationsNeverProvisionedPki(period, institute, reportView));
    reports.addAll(createAmountRequestsModifiedSucceededPki(period, institute, reportView));
    reports.addAll(createAmountRequestsCancelSucceededPki(period, institute, reportView));

    return reports;
  }

  Collection<ErInsertReportDocument> createAmountRequestsCancelSucceededPki(YearMonth previousMonth,
      Optional<String> institute, ReservationReportView reportView) {

    List<ErInsertReportDocument> reports = new ArrayList<>();

    long valuePos = reportView.getAmountRequestsCancelSucceeded();
    reports.add(getVersRequest("Reservation Cancelled", valuePos, previousMonth, institute, "Succeeded"));

    long valueNeg = reportView.getAmountRequestsCancelFailed();
    reports.add(getVersRequest("Reservation Cancelled", valueNeg, previousMonth, institute, "Failed"));

    return reports;
  }

  Collection<ErInsertReportDocument> createAmountRequestsModifiedSucceededPki(YearMonth previousMonth,
      Optional<String> institute, ReservationReportView reportView) {

    List<ErInsertReportDocument> reports = new ArrayList<>();

    long valuePos = reportView.getAmountRequestsModifiedSucceeded();
    reports.add(getVersRequest("Reservation modified", valuePos, previousMonth, institute, "Succeeded"));

    long valueNeg = reportView.getAmountRequestsModifiedFailed();
    reports.add(getVersRequest("Reservation modified", valueNeg, previousMonth, institute, "Failed"));

    return reports;
  }

  Collection<ErInsertReportDocument>  createAmountRunningReservationsNeverProvisionedPki(YearMonth previousMonth,
      Optional<String> institute, ReservationReportView reportView) {

    List<ErInsertReportDocument> reports = new ArrayList<>();

    long valuePos = reportView.getAmountRunningReservationsNeverProvisioned();
    reports.add(getVersRequest("Never provisioned", valuePos, previousMonth, institute, "Timed out"));

    return reports;
  }

  Collection<ErInsertReportDocument> createAmountRequestsThroughGUIPki(YearMonth previousMonth,
    Optional<String> institute, ReservationReportView reportView) {

    List<ErInsertReportDocument> reports = new ArrayList<>();

    long valuePos = reportView.getAmountRequestsThroughNSI();
    reports.add(getVersRequest("Reservations through", valuePos, previousMonth, institute, "NSI"));

    long valueNeg = reportView.getAmountRequestsThroughGUI();
    reports.add(getVersRequest("Reservations through", valueNeg, previousMonth, institute, "GUI"));

    return reports;
  }

  Collection<ErInsertReportDocument> createAmountRunningReservationsStillRunningPki(
    YearMonth previousMonth, Optional<String> institute, ReservationReportView reportView) {

    final String pkiName = "Active Reservations: Running";

    List<ErInsertReportDocument> reports = new ArrayList<>();

    reports.add(
      getVersRequest(pkiName, reportView.getAmountRunningReservationsStillRunning(), previousMonth, institute, "Running"));

    reports.add(
      getVersRequest(pkiName, reportView.getAmountRunningReservationsSucceeded(), previousMonth, institute, "Execution succeeded"));

    reports.add(
      getVersRequest(pkiName, reportView.getAmountRunningReservationsFailed(), previousMonth, institute, "Execution failed"));

    reports.add(
      getVersRequest(pkiName, reportView.getAmountRunningReservationsStillScheduled(), previousMonth, institute, "Scheduled"));

    return reports;
  }

  Collection<ErInsertReportDocument> createRunningReservationsSucceededPki(YearMonth previousMonth,
      Optional<String> institute, ReservationReportView reportView) {

    List<ErInsertReportDocument> reports = new ArrayList<>();

    long valuePos = reportView.getAmountRequestsCreatedSucceeded();
    reports.add(getVersRequest("Reservations created", valuePos, previousMonth, institute, "Succeeded"));

    long valueNeg = reportView.getAmountRequestsCreatedFailed();
    reports.add(getVersRequest("Reservations created", valueNeg, previousMonth, institute, "Failed"));

    return reports;
  }

  Collection<ErInsertReportDocument> createAmountReservationsProtectedPki(YearMonth period,
      Optional<String> institute, ReservationReportView reservationReportView) {

    final String pkiName = "Reservations: Protection Types";

    List<ErInsertReportDocument> reports = new ArrayList<>();

    long valuePos = reservationReportView.getAmountReservationsProtected();
    reports.add(getVersRequest(pkiName, valuePos, period, institute, "Protected Scheduled"));

    long valueNeg = reservationReportView.getAmountReservationsUnprotected();
    reports.add(getVersRequest(pkiName, valueNeg, period, institute, "Unprotected"));

    reports.add(
      getVersRequest(pkiName, reservationReportView.getAmountReservationsRedundant(), period, institute, "Redundant"));

    return reports;
  }

  private void submitReports(Collection<ErInsertReportDocument> reports) {
    try {
      SURFnetErStub stub = new SURFnetErStub(this.serviceURL);

      for (ErInsertReportDocument report : reports) {
        ErInsertReportResponse response = stub.er_InsertReport(report).getErInsertReportResponse();
        if (response.getReturnCode() < 0) {
          log.warn("Sending report failed with {}, '{}'", response.getReturnCode(), response.getReturnText());
        }
      }
    } catch (RemoteException e) {
      log.error("Could not send reports", e);
    }
  }

  protected Map<Optional<String>, ReservationReportView> getAllReservationReportViews(YearMonth period) {
    Map<Optional<String>, ReservationReportView> reportViews = new HashMap<>();

    Collection<PhysicalResourceGroup> prgWithPorts = physicalResourceGroupService.findAllWithPorts();

    for (PhysicalResourceGroup physicalResourceGroup : prgWithPorts) {
      Optional<String> orgName = Optional.of(physicalResourceGroup.getInstitute().getShortName());
      ReservationReportView report = reportingService.determineReportForAdmin(period.toInterval(), BodRole.createManager(physicalResourceGroup));

      reportViews.put(orgName, report);
    }

    reportViews.put(Optional.<String> absent(), reportingService.determineReportForNoc(period.toInterval()));

    return reportViews;
  }

  protected ErInsertReportDocument getVersRequest(
      String type, long value, YearMonth period,
      Optional<String> instituteShortName, String instance) {

    InsertReportInput insertReportInput = InsertReportInput.Factory.newInstance();
    insertReportInput.setNormComp("=");
    insertReportInput.setNormValue(Long.toString(value));
    insertReportInput.setDepartmentList(DEPARTMENT_NETWERK_DIENSTEN);
    insertReportInput.setIsKPI(true);
    insertReportInput.setValue(Long.toString(value));
    insertReportInput.setPeriod(period.toString("yyyy-MM"));
    insertReportInput.setType(type);
    insertReportInput.setInstance(instance);

    if (instituteShortName.isPresent()) {
      insertReportInput.setOrganisation(instituteShortName.get());
      insertReportInput.setIsHidden(false);
    }
    else {
      insertReportInput.setIsHidden(true);
    }

    ErInsertReportDocument versRequest = ErInsertReportDocument.Factory.newInstance();
    versRequest.setErInsertReport(getErInsertReport(insertReportInput));

    return versRequest;
  }

  private ErInsertReport getErInsertReport(final InsertReportInput reportData) {
    ErInsertReport messageBody = ErInsertReport.Factory.newInstance();
    messageBody.setUsername(versUserName);
    messageBody.setPassword(versUserPassword);
    messageBody.setParameters(reportData);

    return messageBody;
  }

  protected void setVersUserName(String versUserName) {
    this.versUserName = versUserName;
  }

  protected void setVersUserPassword(String versUserPassword) {
    this.versUserPassword = versUserPassword;
  }

  protected void setServiceURL(String serviceURL) {
    this.serviceURL = serviceURL;
  }

}