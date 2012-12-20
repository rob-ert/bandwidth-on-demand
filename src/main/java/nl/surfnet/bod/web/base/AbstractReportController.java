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
package nl.surfnet.bod.web.base;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import nl.surfnet.bod.service.ReportingService;
import nl.surfnet.bod.web.view.ReportIntervalView;
import nl.surfnet.bod.web.view.ReservationReportView;

import org.joda.time.Interval;
import org.joda.time.YearMonth;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

public abstract class AbstractReportController {

  protected static final int AMOUNT_OF_REPORT_PERIODS = 8;

  @Resource
  private ReportingService reportingService;

  @RequestMapping(method = RequestMethod.GET)
  public String index(Model model) {
    return index(ReportIntervalView.idFormatter.print(YearMonth.now()), model);
  }

  @RequestMapping(value = "/{selectedYearMonth}", method = RequestMethod.GET)
  public String index(@PathVariable final String selectedYearMonth, Model model) {

    Optional<YearMonth> yearMonth = parseYearMonth(selectedYearMonth);
    ReportIntervalView selectedInterval = new ReportIntervalView(yearMonth.or(YearMonth.now()).toInterval());

    List<ReportIntervalView> intervals = determineReportIntervals();
    if (!intervals.contains(selectedInterval)) {
      intervals.add(selectedInterval);
    }

    model.addAttribute("intervalList", intervals);
    model.addAttribute("baseReportIntervalUrl", getPageUrl());
    model.addAttribute("selectedInterval", selectedInterval);
    model.addAttribute("report", determineReport(selectedInterval.getInterval()));

    return getPageUrl();
  }

  private Optional<YearMonth> parseYearMonth(String yearMonth) {
    try {
      return Optional.of(YearMonth.parse(yearMonth, ReportIntervalView.idFormatter));
    } catch (IllegalArgumentException e) {
      return Optional.absent();
    }
  }

  @VisibleForTesting
  List<ReportIntervalView> determineReportIntervals() {
    final List<ReportIntervalView> reportIntervals = new ArrayList<>();

    for (int i = 0; i < AMOUNT_OF_REPORT_PERIODS; i++) {
      Interval interval  = YearMonth.now().minusMonths(i).toInterval();
      reportIntervals.add(new ReportIntervalView(interval));
    }

    return reportIntervals;
  }

  protected ReportingService getReportingService() {
    return reportingService;
  }

  @RequestMapping("/graph")
  public String graph(Model model) {
    model.addAttribute("dataUrl", getPageUrl() + "/data.csv");
    return getPageUrl() + "/graph";
  }

  @RequestMapping(value = "/data", method = RequestMethod.GET)
  @ResponseBody
  public String graphData(HttpServletResponse response) {
    response.setContentType("text/plain");

    StringBuffer responseBuffer = new StringBuffer("Month,Create,Create_f,Cancel,Cancel_f,NSI,NSI_f").append("\n");

    YearMonth currentMonth = YearMonth.now();
    for (int i = 4; i >= 0; i--) {
      final YearMonth month = currentMonth.minusMonths(i);
      ReservationReportView report = determineReport(month.toInterval());
      addReport(responseBuffer, report, month.toString("MMM"));
    }

    return responseBuffer.toString();
  }

  private void addReport(StringBuffer buffer, ReservationReportView report, String month) {
    buffer.append(month).append(",");
    buffer.append(report.getAmountRequestsCreatedSucceeded()).append(",");
    buffer.append(report.getAmountRequestsCreatedFailed()).append(",");
    buffer.append(report.getAmountRequestsCancelSucceeded()).append(",");
    buffer.append(report.getAmountRequestsCancelFailed()).append(",");
    buffer.append(report.getAmountRequestsThroughNSI()).append(",");
    buffer.append(report.getAmountRequestsThroughGUI()).append("\n");
  }

  protected abstract String getPageUrl();

  protected abstract ReservationReportView determineReport(Interval interval);

}