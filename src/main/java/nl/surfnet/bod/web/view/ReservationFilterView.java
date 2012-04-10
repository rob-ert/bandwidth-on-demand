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

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.ReadablePeriod;

import com.google.common.base.Objects;

/**
 * View object which holds filter related data regarding Reservations.
 *
 * @author Franky
 *
 */
public class ReservationFilterView {

  private final String id;
  private final String label;
  private LocalDateTime start;
  private LocalDateTime end;
  private boolean filterOnReservationEndOnly;

  /**
   * Constructs a filter for the given year. Filtering will take place both on
   * the start and end of Reservation
   *
   * @param year
   *          Year
   */
  public ReservationFilterView(int year) {
    id = String.valueOf(year);
    label = id;

    start = new DateMidnight().withYear(year).withMonthOfYear(DateTimeConstants.JANUARY).withDayOfMonth(01)
        .toDateTime().toLocalDateTime();

    end = new DateMidnight().withYear(year).withMonthOfYear(DateTimeConstants.DECEMBER).withDayOfMonth(31).toDateTime()
        .toLocalDateTime();

    filterOnReservationEndOnly = false;
  }

  /**
   * Constructs a filter for a given Period. Filtering will take place on the
   * end of Reservation only;
   *
   * @param id
   *          Id
   * @param label
   *          Label
   * @param period
   *          Period
   * @param endInPast
   *          Indicates that the endDate lies before the start date. This will
   *          be correct in the filter.
   */
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

    filterOnReservationEndOnly = true;

  }

  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, label);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj instanceof ReservationFilterView) {
      ReservationFilterView resFilterView = (ReservationFilterView) obj;

      return Objects.equal(this.id, resFilterView.id) && Objects.equal(this.label, resFilterView.label);
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

  public boolean isFilterOnReservationEndOnly() {
    return filterOnReservationEndOnly;
  }

  @Override
  public String toString() {
    return id;
  }

  public LocalDate getStartAsLocalDate() {
    return start.toLocalDate();
  }

  public LocalTime getStartAsLocalTime() {
    return start.toLocalTime();
  }

  public LocalDate getEndAsLocalDate() {
    return end.toLocalDate();
  }

  public LocalTime getEndAsLocalTime() {
    return end.toLocalTime();
  }
}