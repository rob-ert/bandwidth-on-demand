package nl.surfnet.bod.web.view;

import java.util.concurrent.atomic.AtomicLong;

import org.hibernate.util.EqualsHelper;
import org.joda.time.LocalDateTime;

import com.google.common.base.Objects;

public class ReservationFilterView {

  private final static AtomicLong idCounter = new AtomicLong();

  private final Long id = idCounter.incrementAndGet();
  private final String labelKey;
  private final String labelValue;
  private final LocalDateTime startPeriod;
  private final LocalDateTime endPeriod;

  public ReservationFilterView(int year) {
    labelKey = null;
    labelValue = String.valueOf(year);
    startPeriod = new LocalDateTime(year, 01, 01, 0, 0, 0, 0);
    endPeriod = new LocalDateTime(year, 12, 31, 0, 0, 0, 0);
  }

  public ReservationFilterView(String labelKey, LocalDateTime startPeriod, LocalDateTime endPeriod) {
    this.labelKey = labelKey;
    this.labelValue = null;
    this.startPeriod = startPeriod;
    this.endPeriod = endPeriod;
  }

  public Long getId() {
    return id;
  }

  public String getLabelKey() {
    return labelKey;
  }

  public String getLabelValue() {
    return labelValue;
  }

  public LocalDateTime getStartPeriod() {
    return startPeriod;
  }

  public LocalDateTime getEndPeriod() {
    return endPeriod;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(labelKey, labelValue, startPeriod, endPeriod);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj instanceof ReservationFilterView) {
      ReservationFilterView resFilterView = (ReservationFilterView) obj;

      return Objects.equal(this.labelKey, resFilterView.labelKey)
          && Objects.equal(this.labelValue, resFilterView.labelValue)
          && Objects.equal(this.startPeriod, resFilterView.startPeriod)
          && Objects.equal(this.endPeriod, resFilterView.endPeriod);
    }
    else {
      return false;
    }
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).toString();
  }
}