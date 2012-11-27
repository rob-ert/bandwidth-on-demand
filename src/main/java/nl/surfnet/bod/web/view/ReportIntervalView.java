package nl.surfnet.bod.web.view;

import org.joda.time.Interval;

public class ReportIntervalView {

  private final int id;
  private final Interval interval;
  private final String label;

  public ReportIntervalView(Interval interval, String label) {
    super();
    this.interval = interval;
    this.label = label;

    // Id will be 201210 when start is oct 2012
    this.id = (interval.getStart().getYear() * 100) + interval.getStart().getMonthOfYear();
  }

  public int getId() {
    return id;
  }

  public Interval getInterval() {
    return interval;
  }

  public String getLabel() {
    return label;
  }

}
