/**
 * Copyright (c) 2012, 2013 SURFnet BV
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
package nl.surfnet.bod.web.view;

import nl.surfnet.bod.domain.ReservationStatus;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.ReadablePeriod;

import com.google.common.base.Objects;

/**
 * View object which holds filter related data regarding Reservations.
 * 
 */
public class ReservationFilterView {

  private final String id;
  private final String label;
  private DateTime start;
  private DateTime end;
  private final boolean filterOnReservationEndOnly;
  private final ReservationStatus[] states;
  private final boolean filterOnStatusOnly;

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
        .toDateTime().toDateTime();

    end = new DateMidnight().withYear(year).withMonthOfYear(DateTimeConstants.DECEMBER).withDayOfMonth(31).toDateTime()
        .toDateTime();

    filterOnReservationEndOnly = false;
    filterOnStatusOnly = false;
    states = ReservationStatus.values();
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
      this.end = DateTime.now();
      this.start = end.minus(period);
      //In the past, all status must be shown, since they cannot be change anymore.
      states = ReservationStatus.values();
    }
    else {
      this.start = DateTime.now();
      this.end = start.plus(period);
      //For the coming period, only show states which can change in the future.
      states = ReservationStatus.TRANSITION_STATES_AS_ARRAY;
    }

    filterOnReservationEndOnly = false;
    filterOnStatusOnly = false;
  }

  /**
   * Constructs a filter based on the specified {@link ReservationStatus}.
   * 
   * @param id
   *          Id
   * @param label
   *          Label
   * @param ReservationStatus
   *          status to filter on
   */
  public ReservationFilterView(String id, String label, ReservationStatus status) {
    this.id = id;
    this.label = label;

    filterOnStatusOnly = true;
    filterOnReservationEndOnly = false;
    this.states = new ReservationStatus[] { status };
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

  public DateTime getStart() {
    return start;
  }

  public DateTime getEnd() {
    return end;
  }

  public ReservationStatus[] getStates() {
    return states;
  }

  public boolean isFilterOnReservationEndOnly() {
    return filterOnReservationEndOnly;
  }

  public boolean isFilterOnStatusOnly() {
    return filterOnStatusOnly;
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