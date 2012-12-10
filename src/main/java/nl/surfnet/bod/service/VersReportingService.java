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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.vers.SURFnetErStub;
import nl.surfnet.bod.web.view.NocStatisticsView;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import surfnet_er.ErInsertReportDocument;
import surfnet_er.ErInsertReportDocument.ErInsertReport;
import surfnet_er.ErInsertReportResponseDocument.ErInsertReportResponse;
import surfnet_er.InsertReportInput;

@Service
public class VersReportingService {

  @Value("${vers.url}")
  private String serviceURL = "https://rapportage-test.surfnet.nl:9011/interface.php";

  @Value("${vers.user}")
  private String versUserName;

  @Value("${vers.password}")
  private String versUserPassword;

  @Resource
  private PhysicalPortService physicalPortService;

  @Resource
  private ReservationService reservationService;

  private SURFnetErStub surFnetErStub;

  @SuppressWarnings("unused")
  private final Logger log = LoggerFactory.getLogger(getClass());

  @PostConstruct
  void init() throws IOException {
    surFnetErStub = new SURFnetErStub(serviceURL);
  }

  public VersResponse sendActiveReservationsReportToAll() throws IOException {
    return sendReportToAll("Active reservations", "=", Long.toString(getNocStatistics().getActiveReservationsAmount()));
  }

  public VersResponse sendUnalignedPhysicalPortsReportToAll() throws IOException {
    return sendReportToAll("Unaligned physical ports", "=",
        Long.toString(getNocStatistics().getUnalignedPhysicalPortsAmount()));
  }

  
  private VersResponse sendReportToAll(final String type, final String delimiter, final String value)
      throws IOException {
    final ErInsertReportDocument versRequest = ErInsertReportDocument.Factory.newInstance();
    final InsertReportInput insertReportInput = InsertReportInput.Factory.newInstance();
    insertReportInput.setType(type);
    insertReportInput.setNormComp(delimiter);
    insertReportInput.setNormValue(value);
    insertReportInput.setDepartmentList("NWD");
    insertReportInput.setIsKPI(true);
    insertReportInput.setIsHidden(false);
    insertReportInput.setPeriod(getReportPeriod());
    versRequest.setErInsertReport(getErInsertReport(insertReportInput));
    final ErInsertReportResponse versRepsonse = surFnetErStub.er_InsertReport(versRequest).getErInsertReportResponse();
    return new VersResponse(versRepsonse.getReturnCode(), versRepsonse.getReturnText());
  }

  private VersResponse sendReportToOrganization(final InsertReportInput insertReportInput, final String iddShortName)
      throws IOException {
    final ErInsertReportDocument versRequest = ErInsertReportDocument.Factory.newInstance();
    insertReportInput.setPeriod(getReportPeriod());
    versRequest.setErInsertReport(getErInsertReport(insertReportInput));
    final ErInsertReportResponse versRepsonse = surFnetErStub.er_InsertReport(versRequest).getErInsertReportResponse();
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

  // TODO: Code duplication, copied from DashboardController
  private NocStatisticsView getNocStatistics() {
    final ReservationFilterViewFactory reservationFilterViewFactory = new ReservationFilterViewFactory();

    final long countPhysicalPorts = physicalPortService.countAllocated();

    final long countElapsedReservations = reservationService.countAllEntriesUsingFilter(reservationFilterViewFactory
        .create(ReservationFilterViewFactory.ELAPSED));

    final long countActiveReservations = reservationService.countAllEntriesUsingFilter(reservationFilterViewFactory
        .create(ReservationFilterViewFactory.ACTIVE));

    final long countComingReservations = reservationService.countAllEntriesUsingFilter(reservationFilterViewFactory
        .create(ReservationFilterViewFactory.COMING));

    final long countMissingPhysicalPorts = physicalPortService.countUnalignedPhysicalPorts();

    final NocStatisticsView nocStatisticsView = new NocStatisticsView(countPhysicalPorts, countElapsedReservations,
        countActiveReservations, countComingReservations, countMissingPhysicalPorts);

    return nocStatisticsView;
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
