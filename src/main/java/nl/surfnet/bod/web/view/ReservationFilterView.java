package nl.surfnet.bod.web.view;

import java.util.concurrent.atomic.AtomicLong;

import nl.surfnet.bod.domain.Reservation;

import org.joda.time.LocalDateTime;

import com.google.common.base.Objects;

/**
 * View object which holds filter related data regaring {@link Reservation}s.
 * 
 * @author Franky
 * 
 */
public class ReservationFilterView {

  private final static AtomicLong idCounter = new AtomicLong();

  private final Long id = idCounter.incrementAndGet();
  private final String label;
  private final LocalDateTime startPeriod;
  private final LocalDateTime endPeriod;
  private final boolean filterOnEndDateOnly;

  public ReservationFilterView(int year) {
    label = String.valueOf(year);
    startPeriod = new LocalDateTime(year, 01, 01, 0, 0, 0, 0);
    endPeriod = new LocalDateTime(year, 12, 31, 0, 0, 0, 0);
    filterOnEndDateOnly = false;
  }

  public ReservationFilterView(String label, LocalDateTime startPeriod, LocalDateTime endPeriod) {
    this.label = label;
    this.startPeriod = startPeriod;
    this.endPeriod = endPeriod;
    filterOnEndDateOnly = true;
  }

  public Long getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  public LocalDateTime getStartPeriod() {
    return startPeriod;
  }

  public LocalDateTime getEndPeriod() {
    return endPeriod;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(label, startPeriod, endPeriod, filterOnEndDateOnly);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj instanceof ReservationFilterView) {
      ReservationFilterView resFilterView = (ReservationFilterView) obj;

      return Objects.equal(this.label, resFilterView.label)
          && Objects.equal(this.startPeriod, resFilterView.startPeriod)
          && Objects.equal(this.endPeriod, resFilterView.endPeriod)
          && Objects.equal(this.isFilterOnEndDateOnly(), resFilterView.filterOnEndDateOnly);
    }
    else {
      return false;
    }
  }

  @Override
  public String toString() {
    return label;
  }

  public boolean isFilterOnEndDateOnly() {
    return filterOnEndDateOnly;
  }
}