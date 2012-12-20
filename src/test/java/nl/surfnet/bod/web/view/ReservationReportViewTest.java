package nl.surfnet.bod.web.view;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.joda.time.DateTime;
import org.junit.Test;

public class ReservationReportViewTest {

  private final DateTime periodStart = DateTime.now();
  private final DateTime periodEnd = periodStart.plusHours(1);

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowWhenStartAfterEnd() {
    new ReservationReportView(periodEnd, periodStart);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowWhenStartEqualToEnd() {
    new ReservationReportView(periodStart, periodStart);
  }

  @Test
  public void shouldGetStartAndEnd() {
    ReservationReportView report = new ReservationReportView(periodStart, periodEnd);
    assertThat(report.getPeriodStart(), is(periodStart));
    assertThat(report.getPeriodEnd(), is(periodEnd));
  }

}
