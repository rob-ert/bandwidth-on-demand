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

import java.io.IOException;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import nl.surfnet.bod.vers.SURFnetErStub;
import nl.surfnet.bod.web.view.ReportIntervalView;
import nl.surfnet.bod.web.view.ReservationReportView;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import surfnet_er.ErInsertReportDocument;
import surfnet_er.ErInsertReportDocument.ErInsertReport;
import surfnet_er.ErInsertReportResponseDocument.ErInsertReportResponse;
import surfnet_er.InsertReportInput;

import com.google.common.annotations.VisibleForTesting;

@Service
public class VersReportingService {

  private static final String ORGANIZATION = "BoD";

  @Value("${vers.url}")
  private String serviceURL;

  @Value("${vers.user}")
  private String versUserName;

  @Value("${vers.password}")
  private String versUserPassword;

  @Resource
  private ReportingService reportingService;

  private SURFnetErStub surfNetErStub;

  @SuppressWarnings("unused")
  private final Logger log = LoggerFactory.getLogger(getClass());

  private final String firstDayOfTheMonthCronExpression = "0 0 0 1 * ?";

  @PostConstruct
  void init() throws IOException {
    surfNetErStub = new SURFnetErStub(serviceURL);
  }

  // @Scheduled(cron = firstDayOfTheMonthCronExpression)
  public void sendActiveReservationsRunningReportToAll() throws IOException {

    final DateTime start = LocalDateTime.now().minusMonths(1).toDateTime();
    final DateTime end = LocalDateTime.now().toDateTime();
    final Interval interval = new Interval(start, end);
    final DateTimeFormatter labelFormatter = DateTimeFormat.forPattern("yyyy MMM");

    final ReservationReportView nocReport = reportingService.determineReport(new ReportIntervalView(interval,
        labelFormatter.print(end)), new ArrayList<String>());

    sendReportToAll("Active Reservations Running", "=",
        Long.toString(nocReport.getAmountRunningReservationsStillRunning()));
  }

  @VisibleForTesting
  VersResponse sendReportToAll(final String type, final String delimiter, final String value) throws IOException {
    final ErInsertReportDocument versRequest = ErInsertReportDocument.Factory.newInstance();
    final InsertReportInput insertReportInput = InsertReportInput.Factory.newInstance();
    insertReportInput.setType(type);
    insertReportInput.setNormComp(delimiter);
    insertReportInput.setNormValue(value);
    insertReportInput.setDepartmentList("NWD");
    insertReportInput.setIsKPI(true);
    insertReportInput.setIsHidden(false);
    insertReportInput.setPeriod(getReportPeriod());
    insertReportInput.setOrganisation(ORGANIZATION);
    versRequest.setErInsertReport(getErInsertReport(insertReportInput));
    final ErInsertReportResponse versRepsonse = surfNetErStub.er_InsertReport(versRequest).getErInsertReportResponse();
    return new VersResponse(versRepsonse.getReturnCode(), versRepsonse.getReturnText());
  }

  private VersResponse sendReportToOrganization(final InsertReportInput insertReportInput, final String iddShortName)
      throws IOException {
    final ErInsertReportDocument versRequest = ErInsertReportDocument.Factory.newInstance();
    insertReportInput.setPeriod(getReportPeriod());
    versRequest.setErInsertReport(getErInsertReport(insertReportInput));
    final ErInsertReportResponse versRepsonse = surfNetErStub.er_InsertReport(versRequest).getErInsertReportResponse();
    return new VersResponse(versRepsonse.getReturnCode(), versRepsonse.getReturnText());
  }

  private ErInsertReport getErInsertReport(final InsertReportInput reportData) {
    final ErInsertReport messageBody = ErInsertReport.Factory.newInstance();
    messageBody.setUsername(versUserName);
    messageBody.setPassword(versUserPassword);
    messageBody.setParameters(reportData);
    return messageBody;
  }

  private String getReportPeriod() {
    final LocalDateTime now = LocalDateTime.now();
    final StringBuilder period = new StringBuilder();
    final int monthOfYear = now.getMonthOfYear();
    period.append(now.getYear());
    period.append('-');
    if (monthOfYear <= 9) {
      period.append("0");
    }
    period.append(monthOfYear);
    return period.toString();
  }

  public static class VersResponse {
    private final String errorMessage;
    private final int errorCode;

    public VersResponse(final int errorCode, final String errorMessage) {
      this.errorCode = errorCode;
      this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
      return errorCode;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

  }

}
