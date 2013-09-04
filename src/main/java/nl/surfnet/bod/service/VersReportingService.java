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

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.Map.Entry;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.BodRole;
import nl.surfnet.bod.domain.PhysicalResourceGroup;
import nl.surfnet.bod.vers.SURFnetErStub;
import nl.surfnet.bod.web.view.ReservationReportView;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

@Service
public class VersReportingService {

  private static final String DEPARTMENT_NETWERK_DIENSTEN = "NWD";
  // Every 1st of the month at 1:00am
  private static final String FIRST_DAY_OF_THE_MONTH_CRON_EXPRESSION = "0 0 1 1 1/1 ?";

  @Value("${vers.enabled}") private boolean enabled;
  @Value("${vers.url}") private String serviceURL;
  @Value("${vers.user}") private String versUserName;
  @Value("${vers.password}") private String versUserPassword;
  @Resource private ReportingService reportingService;
  @Resource private PhysicalResourceGroupService physicalResourceGroupService;

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Scheduled(cron = FIRST_DAY_OF_THE_MONTH_CRON_EXPRESSION)
  public void sendInternalReport() throws Exception {
    if (!enabled) {
      logger.info(String.format("Skiping VERS reporting, service is disabled"));
      return;
    }

    YearMonth previousMonth = YearMonth.now().minusMonths(1);
    sendReports(previousMonth);
  }

  @VisibleForTesting
  protected void sendReports(YearMonth period) {
    logger.info(String.format("Sending reports to VERS for '%s'", period.toString("yyyy-MM")));

    Map<Optional<String>, ReservationReportView> reportViews = getAllReservationReportViews(period);

    for (Entry<Optional<String>, ReservationReportView> reservationReportViewEntry : reportViews.entrySet()) {
      Optional<String> institute = reservationReportViewEntry.getKey();
      ReservationReportView reportView = reservationReportViewEntry.getValue();

      Collection<ErInsertReportDocument> reports = createAllReports(period, institute, reportView);

      logger.info(String.format("Sending %d reports for institute '%s'", reports.size(), institute.or("Null/Not set")));

      submitReports(reports);
    }
  }

  /**
   * Simply tests if the wsdl of the vers reporting service can be retrieved
   *
   * @return true if so, false otherwise
   */
  public boolean isWsdlAvailable() {
    try {
      HttpResponse response = new DefaultHttpClient().execute(new HttpGet(serviceURL + "?wsdl"));

      return HttpStatus.SC_OK == response.getStatusLine().getStatusCode();
    } catch (IOException e) {
      logger.warn("Error performing healthcheck on Vers", e);
      return false;
    }
  }

  Collection<ErInsertReportDocument> createAllReports(YearMonth period, Optional<String> institute, ReservationReportView reportView) {
    List<ErInsertReportDocument> reports = new ArrayList<>();

    reports.addAll(createRunningReservationsSucceededPki(period, institute, reportView));
    reports.addAll(createRequestsModifiedSucceededPki(period, institute, reportView));
    reports.addAll(createRequestsCancelSucceededPki(period, institute, reportView));
    reports.addAll(createRequestsThroughPki(period, institute, reportView));
    reports.addAll(createReservationsProtectionTypePki(period, institute, reportView));
    reports.addAll(createServicesPki(period, institute, reportView));

    return reports;
  }

  Collection<ErInsertReportDocument> createRequestsCancelSucceededPki(YearMonth previousMonth,
      Optional<String> institute, ReservationReportView reportView) {

    final String pkiName = "Reservations cancelled";

    List<ErInsertReportDocument> reports = new ArrayList<>();

    reports.add(
        getVersRequest(
            pkiName, reportView.getAmountRequestsCancelSucceeded(), previousMonth, institute, "Succeeded"));

    reports.add(
        getVersRequest(
            pkiName, reportView.getAmountRequestsCancelFailed(), previousMonth, institute, "Failed"));

    return reports;
  }

  Collection<ErInsertReportDocument> createRequestsModifiedSucceededPki(YearMonth previousMonth,
      Optional<String> institute, ReservationReportView reportView) {

    final String pkiName = "Reservations modified";

    List<ErInsertReportDocument> reports = new ArrayList<>();

    reports.add(
        getVersRequest(
            pkiName, reportView.getAmountRequestsModifiedSucceeded(), previousMonth, institute, "Succeeded"));

    reports.add(
        getVersRequest(
            pkiName, reportView.getAmountRequestsModifiedFailed(), previousMonth, institute, "Failed"));

    return reports;
  }

  Collection<ErInsertReportDocument> createRequestsThroughPki(YearMonth previousMonth,
      Optional<String> institute, ReservationReportView reportView) {

    final String pkiName = "Reservations through";

    List<ErInsertReportDocument> reports = new ArrayList<>();

    reports.add(getVersRequest(pkiName, reportView.getAmountRequestsThroughNSI(), previousMonth, institute, "NSI"));

    reports.add(getVersRequest(pkiName, reportView.getAmountRequestsThroughGUI(), previousMonth, institute, "GUI"));

    return reports;
  }

  Collection<ErInsertReportDocument> createServicesPki(
      YearMonth previousMonth, Optional<String> institute, ReservationReportView reportView) {

    final String pkiName = "Services";

    List<ErInsertReportDocument> reports = new ArrayList<>();

    reports.add(
        getVersRequest(
            pkiName, reportView.getAmountRunningReservationsStillRunning(), previousMonth, institute, "Running"));

    reports.add(
        getVersRequest(
        pkiName, reportView.getAmountRunningReservationsSucceeded(), previousMonth, institute, "Execution succeeded"));

    reports.add(
        getVersRequest(
            pkiName, reportView.getAmountRunningReservationsFailed(), previousMonth, institute, "Execution failed"));

    reports.add(
        getVersRequest(
            pkiName, reportView.getAmountRunningReservationsStillScheduled(), previousMonth, institute, "Scheduled"));

    reports.add(
        getVersRequest(
            pkiName, reportView.getAmountRunningReservationsNeverProvisioned(), previousMonth, institute,
            "Never provisioned"));

    return reports;
  }

  Collection<ErInsertReportDocument> createRunningReservationsSucceededPki(YearMonth previousMonth,
      Optional<String> institute, ReservationReportView reportView) {

    final String pkiName = "Reservations created";

    List<ErInsertReportDocument> reports = new ArrayList<>();

    reports.add(
        getVersRequest(
            pkiName, reportView.getAmountRequestsCreatedSucceeded(), previousMonth, institute, "Succeeded"));

    reports.add(
        getVersRequest(
            pkiName, reportView.getAmountRequestsCreatedFailed(), previousMonth, institute, "Failed"));

    return reports;
  }

  Collection<ErInsertReportDocument> createReservationsProtectionTypePki(YearMonth period,
      Optional<String> institute, ReservationReportView reservationReportView) {

    final String pkiName = "Reservation protection type";

    List<ErInsertReportDocument> reports = new ArrayList<>();

    reports.add(
        getVersRequest(
            pkiName, reservationReportView.getAmountReservationsProtected(), period, institute, "Protected"));

    reports.add(
        getVersRequest(
            pkiName, reservationReportView.getAmountReservationsUnprotected(), period, institute, "Unprotected"));

    reports.add(
        getVersRequest(
            pkiName, reservationReportView.getAmountReservationsRedundant(), period, institute, "Redundant"));

    return reports;
  }

  private void submitReports(Collection<ErInsertReportDocument> reports) {
    try {
      SURFnetErStub stub = new SURFnetErStub(this.serviceURL);

      for (ErInsertReportDocument report : reports) {
        ErInsertReportResponse response = stub.er_InsertReport(report).getErInsertReportResponse();
        if (response.getReturnCode() < 0) {
          logger.warn("Sending report failed with {}, '{}'", response.getReturnCode(), response.getReturnText());
        }
      }
    } catch (RemoteException e) {
      logger.error("Could not send reports", e);
    }
  }

  protected Map<Optional<String>, ReservationReportView> getAllReservationReportViews(YearMonth period) {
    Map<Optional<String>, ReservationReportView> reportViews = new HashMap<>();

    Collection<PhysicalResourceGroup> prgWithPorts = physicalResourceGroupService.findAllWithPorts();

    for (PhysicalResourceGroup physicalResourceGroup : prgWithPorts) {
      Optional<String> orgName = Optional.of(physicalResourceGroup.getInstitute().getShortName());
      ReservationReportView report = reportingService.determineReportForAdmin(period.toInterval(), BodRole
          .createManager(physicalResourceGroup));

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
    } else {
      insertReportInput.setIsHidden(true);
    }

    ErInsertReportDocument versRequest = ErInsertReportDocument.Factory.newInstance();
    versRequest.setErInsertReport(getErInsertReport(insertReportInput));

    return versRequest;
  }

  private ErInsertReport getErInsertReport(InsertReportInput reportData) {
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