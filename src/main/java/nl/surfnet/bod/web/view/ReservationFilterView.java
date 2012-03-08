package nl.surfnet.bod.web.view;

import org.joda.time.LocalDateTime;

public class ReservationFilterView {

  private final Long id;
  private final String labelKey;
  private final LocalDateTime startPeriod;
  private final LocalDateTime endPeriod;

  public ReservationFilterView(Long id, String labelKey, LocalDateTime startPeriod, LocalDateTime endPeriod) {
    this.id = id;
    this.labelKey = labelKey;
    this.startPeriod = startPeriod;
    this.endPeriod = endPeriod;
  }

  public Long getId() {
    return id;
  }

  public String getLabelKey() {
    return labelKey;
  }

  public LocalDateTime getStartPeriod() {
    return startPeriod;
  }

  public LocalDateTime getEndPeriod() {
    return endPeriod;
  }
}