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
  private LocalDateTime start;
  private LocalDateTime end;

  public ReservationFilterView(int year) {
    id = String.valueOf(year);
    label = id;

    start = new DateMidnight().withYear(year).withMonthOfYear(DateTimeConstants.JANUARY).withDayOfMonth(01)
        .toDateTime().toLocalDateTime();

    end = new DateMidnight().withYear(year).withMonthOfYear(DateTimeConstants.DECEMBER).withDayOfMonth(31).toDateTime()
        .toLocalDateTime();
  }

  public ReservationFilterView(String id, String label, ReadablePeriod period, boolean endInPast) {
    this.id = id;
    this.label = label;

    if (endInPast) {
      this.end = LocalDateTime.now();
      this.start = end.minus(period);
    }
    else {
      this.start = LocalDateTime.now();
      this.end = start.plus(period);
    }
  }

  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, label, start, end);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj instanceof ReservationFilterView) {
      ReservationFilterView resFilterView = (ReservationFilterView) obj;

      return Objects.equal(this.id, resFilterView.id) && Objects.equal(this.label, resFilterView.label)
          && Objects.equal(this.start, resFilterView.start) && Objects.equal(this.end, resFilterView.getEnd());
    }
    else {
      return false;
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
    return start.toLocalDate();
  }

  public LocalDate getEndAsLocalDate() {
    return end.toLocalDate();
  }
}