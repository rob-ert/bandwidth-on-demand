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

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import nl.surfnet.bod.vers.SURFnetErStub;
import nl.surfnet.bod.web.view.ReportIntervalView;
import nl.surfnet.bod.web.view.ReservationReportView;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
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
import surfnet_er.ErInsertTypeDocument;
import surfnet_er.ErInsertTypeDocument.ErInsertType;
import surfnet_er.ErInsertTypeResponseDocument;
import surfnet_er.InsertReportInput;
import surfnet_er.InsertTypeInput;

import com.google.common.base.Optional;

@Service
public class VersReportingService {

  // public static final String DEFAULT_ORGANIZATION = "SURFNET";

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

  private SURFnetErStub surfNetErStub;

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final String firstDayOfTheMonthCronExpression = "0 0 0 1 * ?";

  private final DateTimeFormatter versFormatter = DateTimeFormat.forPattern("yyyy-MM");
  private final DateTimeFormatter bodLabelFormatter = DateTimeFormat.forPattern("yyyy MMM");

  @PostConstruct
  void init() throws IOException {
    surfNetErStub = new SURFnetErStub(serviceURL);
  }

  // @Scheduled(cron = firstDayOfTheMonthCronExpression)
  public List<VersResponse> sendReportToAll(final DateTime start) throws Exception {

    final VersReportPeriod versReportPeriod = new VersReportPeriod();
    final ReservationReportView nocReport = reportingService.determineReport(
        new ReportIntervalView(versReportPeriod.getInterval(), bodLabelFormatter.print(versReportPeriod.getStart())),
        new ArrayList<String>());

    @SuppressWarnings("unchecked")
    final Map<String, String> nocReportValues = BeanUtils.describe(nocReport);
    final List<VersResponse> versResponses = new ArrayList<>();

    for (final Entry<String, String> entry : nocReportValues.entrySet()) {
      final String value = entry.getValue();
      final String humanReadableKey = String.format("%s", camelCaseToHumanReadable(entry.getKey()));
      if (StringUtils.isNumeric(value)) {
        final ErInsertReportDocument versRequest = getVersRequest(humanReadableKey, "=", value, start, Optional.<String>absent());
        final ErInsertReportResponse versRepsonse = surfNetErStub.er_InsertReport(versRequest)
            .getErInsertReportResponse();
        versResponses.add(new VersResponse(versRepsonse.getReturnCode(), versRepsonse.getReturnText()));
      }
      else {
        log.warn("Unable to send attribute {} with value {} to VERS", humanReadableKey, value);
      }
    }

    // final Collection<Institute> institutes =
    // instituteIddService.findAlignedWithIDD();
    // for (final Institute institute : institutes) {
    // final ReservationReportView adminReport =
    // reportingService.determineReport(new ReportIntervalView(
    // versReportPeriod.getInterval(),
    // bodLabelFormatter.print(versReportPeriod.getStart())), institute
    // .getAdminGroups());
    // sendReportToOrganization("Active Reservations Scheduled", "=",
    // Long.toString(adminReport.getAmountRunningReservationsStillScheduled()),
    // versReportPeriod.getStart(),
    // institute.getShortName());
    // }

    return versResponses;
  }

  // private VersResponse sendReportToOrganization(final String type, final
  // String delimiter, final String value,
  // DateTime start, final String instituteShortName) throws IOException {
  // final ErInsertReportDocument versRequest = getVersRequest(type, delimiter,
  // value, start, instituteShortName);
  // final ErInsertReportResponse versRepsonse =
  // surfNetErStub.er_InsertReport(versRequest).getErInsertReportResponse();
  // return new VersResponse(versRepsonse.getReturnCode(),
  // versRepsonse.getReturnText());
  // }

  private ErInsertReportDocument getVersRequest(final String type, final String delimiter, final String value,
      DateTime start, Optional<String> instituteShortName) {
    final ErInsertReportDocument versRequest = ErInsertReportDocument.Factory.newInstance();
    final InsertReportInput insertReportInput = InsertReportInput.Factory.newInstance();
    insertReportInput.setType(type);
    insertReportInput.setNormComp(delimiter);
    insertReportInput.setNormValue(value);
    insertReportInput.setDepartmentList("NWD");
    insertReportInput.setIsKPI(true);
    insertReportInput.setValue(value);
    insertReportInput.setPeriod(versFormatter.print(start.toLocalDateTime()));

    // insertReportInput.setInstance(DEFAULT_ORGANIZATION + " Prod");

    if (instituteShortName.isPresent()) {
      insertReportInput.setOrganisation(instituteShortName.get());
      insertReportInput.setIsHidden(false);
    }
    else {
      insertReportInput.setIsHidden(true);
    }
    versRequest.setErInsertReport(getErInsertReport(insertReportInput));
    return versRequest;
  }

  public VersResponse insertType(final String type, final String delimiter, final String value) throws IOException {
    final ErInsertTypeDocument versRequest = ErInsertTypeDocument.Factory.newInstance();
    final ErInsertType insertType = ErInsertType.Factory.newInstance();
    insertType.setUsername(versUserName);
    insertType.setPassword(versUserPassword);
    final InsertTypeInput insertReportInput = insertType.addNewParameters();

    insertReportInput.setType(type);
    insertReportInput.setNormComp(delimiter);
    insertReportInput.setNormValue(value);
    insertReportInput.setDepartmentList("NWD");
    insertReportInput.setIsKPI(true);
    final ErInsertTypeResponseDocument versResponse = surfNetErStub.er_InsertType(versRequest);
    return new VersResponse(versResponse.getErInsertTypeResponse().getReturnCode(), versResponse
        .getErInsertTypeResponse().getReturnText());

  }

  private ErInsertReport getErInsertReport(final InsertReportInput reportData) {
    final ErInsertReport messageBody = ErInsertReport.Factory.newInstance();
    messageBody.setUsername(versUserName);
    messageBody.setPassword(versUserPassword);
    messageBody.setParameters(reportData);
    return messageBody;
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

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("VersResponse [errorMessage=");
      builder.append(errorMessage);
      builder.append(", errorCode=");
      builder.append(errorCode);
      builder.append("]");
      return builder.toString();
    }
  }

  public class VersReportPeriod {

    private final DateTime start = LocalDateTime.now().minusMonths(1).toDateTime();
    private final DateTime end = LocalDateTime.now().toDateTime();
    private final Interval interval = new Interval(start, end);

    public final DateTime getStart() {
      return start;
    }

    public final DateTime getEnd() {
      return end;
    }

    public final Interval getInterval() {
      return interval;
    }

  }

  private static String camelCaseToHumanReadable(final String s) {
    final char[] chars = s.replaceAll(
        String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])"),
        " ").toCharArray();
    chars[0] = Character.toUpperCase(chars[0]);
    return new String(chars);
  }

  public static void main(String args[]) throws Exception {
    final ReservationReportView reservationReportViewNoc = new ReservationReportView(DateTime.now(), DateTime.now()
        .plusHours(1));
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
