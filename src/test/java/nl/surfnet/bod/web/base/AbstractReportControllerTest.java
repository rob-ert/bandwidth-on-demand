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
package nl.surfnet.bod.web.base;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;

import nl.surfnet.bod.support.ModelStub;
import nl.surfnet.bod.web.view.ReportIntervalView;
import nl.surfnet.bod.web.view.ReservationReportView;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.YearMonth;
import org.junit.Test;

public class AbstractReportControllerTest {

  private final TestReportController subject = new TestReportController();

  @Test
  public void shouldHaveIntervalsUtilNow() {
    final List<ReportIntervalView> intervals = subject.determineReportIntervals();

    assertThat(intervals, hasSize(TestReportController.AMOUNT_OF_REPORT_PERIODS));

    DateTime firstDay;
    for (int i = 0; i < TestReportController.AMOUNT_OF_REPORT_PERIODS; i++) {
      firstDay = YearMonth.now().toLocalDate(1).toDateTimeAtStartOfDay().minusMonths(i);

      assertThat("Month start: " + i, intervals.get(i).getInterval().getStart(), is(firstDay));
      assertThat("Month end: " + i, intervals.get(i).getInterval().getEnd(), is(firstDay.plusMonths(1)));

      assertThat("Month id: " + i, String.valueOf(intervals.get(i).getId()), is(String
          .valueOf(firstDay.getYear())
          + (firstDay.getMonthOfYear() < 10 ? "0" : "") + String.valueOf(firstDay.getMonthOfYear())));
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void showReportOfLongTimeAgoShouldAddTheSelectedInterval() {
    ModelStub model = new ModelStub();

    subject.index("201001", model);

    assertThat((Collection<ReportIntervalView>) model.asMap().get("intervalList"), hasSize(AbstractReportController.AMOUNT_OF_REPORT_PERIODS + 1));
    assertThat((ReportIntervalView) model.asMap().get("selectedInterval"), is(new ReportIntervalView(new YearMonth(2010, 1).toInterval())));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void showReportOfCurrentMonthShouldNotAddTheSelectedInterval() {
    ModelStub model = new ModelStub();

    subject.index(ReportIntervalView.ID_FORMATTER.print(YearMonth.now()), model);

    assertThat((Collection<ReportIntervalView>) model.asMap().get("intervalList"), hasSize(AbstractReportController.AMOUNT_OF_REPORT_PERIODS));
    assertThat((ReportIntervalView) model.asMap().get("selectedInterval"), is(new ReportIntervalView(YearMonth.now().toInterval())));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void showReportWithAnIllegalIntervalIdShouldShowNow() {
    ModelStub model = new ModelStub();

    subject.index("23asdf324asdf", model);

    assertThat((Collection<ReportIntervalView>) model.asMap().get("intervalList"), hasSize(AbstractReportController.AMOUNT_OF_REPORT_PERIODS));
    assertThat((ReportIntervalView) model.asMap().get("selectedInterval"), is(new ReportIntervalView(YearMonth.now().toInterval())));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void showReportWithoutIntervalIdShouldShowNow() {
    ModelStub model = new ModelStub();

    subject.index(model);

    assertThat((Collection<ReportIntervalView>) model.asMap().get("intervalList"), hasSize(AbstractReportController.AMOUNT_OF_REPORT_PERIODS));
    assertThat((ReportIntervalView) model.asMap().get("selectedInterval"), is(new ReportIntervalView(YearMonth.now().toInterval())));
  }

  private class TestReportController extends AbstractReportController {

    @Override
    protected String getPageUrl() {
      return "test";
    }

    @Override
    protected ReservationReportView determineReport(Interval interval) {
      return new ReservationReportView(interval.getStart(), interval.getEnd());
    }

  }

}
