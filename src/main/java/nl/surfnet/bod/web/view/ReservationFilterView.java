/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.web.view;

import nl.surfnet.bod.domain.Reservation;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.ReadablePeriod;
import org.joda.time.Years;

import com.google.common.base.Objects;

/**
 * View object which holds filter related data regaring {@link Reservation}s.
 * 
 * @author Franky
 * 
 */
public class ReservationFilterView {

  private final String id;
  private final String label;
  private final ReadablePeriod period;
  private final boolean endInPast;

  private LocalDateTime start;
  private LocalDateTime end;

  public ReservationFilterView(int year) {
    id = String.valueOf(year);
    label = id;
    endInPast = false;

    start = new DateMidnight().withYear(year).withMonthOfYear(DateTimeConstants.JANUARY).withDayOfMonth(01)
        .toDateTime().toLocalDateTime();

    end = new DateMidnight().withYear(year).withMonthOfYear(DateTimeConstants.DECEMBER).withDayOfMonth(31).toDateTime()
        .toLocalDateTime();

    period = Years.ONE;

  }

  public ReservationFilterView(String id, String label, ReadablePeriod period, boolean endInPast) {
    this.id = id;
    this.label = label;
    this.period = period;
    this.start = null;
    this.endInPast = endInPast;
  }

  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, label, period, start);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj instanceof ReservationFilterView) {
      ReservationFilterView resFilterView = (ReservationFilterView) obj;

      return Objects.equal(this.id, resFilterView.id) && Objects.equal(this.label, resFilterView.label)
          && Objects.equal(this.period, resFilterView.period) && Objects.equal(this.start, resFilterView.start);

    }
    else {
      return false;
    }
  }

  public void resetStartAndEnd() {
    LocalDateTime calculatedStart;
    if (start == null) {
      calculatedStart = LocalDateTime.now();
    }
    else {
      calculatedStart = start;
    }

    if (endInPast) {
      this.end = calculatedStart;
      this.start = calculatedStart.plus(period);
    }
    else {
      this.start = calculatedStart;
      this.end = calculatedStart.plus(period);
    }
  }

  public LocalDateTime getStart() {
    return start;
  }

  public LocalDateTime getEnd() {
    return end;
  }

  @Override
  public String toString() {
    return id + " " + label;
  }

  public LocalDate getStartAsLocalDate() {
    return start == null ? null : start.toLocalDate();
  }

  public LocalDate getEndAsLocalDate() {
    return end == null ? null : end.toLocalDate();
  }
}