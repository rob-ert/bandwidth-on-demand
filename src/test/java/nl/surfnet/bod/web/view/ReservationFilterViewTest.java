/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.web.view;

import nl.surfnet.bod.domain.ReservationStatus;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.junit.Test;

import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;

public class ReservationFilterViewTest {

  private ReservationFilterView reservationFilterView;

  @Test
  public void testYearWithLocalDate() {
    reservationFilterView = new ReservationFilterView(2012);
    assertThat(reservationFilterView.getStartAsLocalDate(), is(new LocalDate().withYear(2012).withMonthOfYear(
        DateTimeConstants.JANUARY).withDayOfMonth(01)));

    assertThat(reservationFilterView.getEndAsLocalDate(), is(new LocalDate().withYear(2012).withMonthOfYear(
        DateTimeConstants.DECEMBER).withDayOfMonth(31)));

    assertThat(Lists.newArrayList(reservationFilterView.getStates()), hasItems(ReservationStatus.values()));

    assertFalse(reservationFilterView.isFilterOnReservationEndOnly());
  }

  @Test
  public void testCommingPeriod() {
    DateTime now = DateTime.now();

    try {
      DateTimeUtils.setCurrentMillisFixed(now.toDateTime().getMillis());
      reservationFilterView = new ReservationFilterView("testId", "testLabel", Months.THREE, false);

      assertThat(reservationFilterView.getStart(), is(now));
      assertThat(reservationFilterView.getEnd(), is(now.plus(Months.THREE)));
      assertThat(Lists.newArrayList(reservationFilterView.getStates()),
          hasItems(ReservationStatus.TRANSITION_STATES_AS_ARRAY));
      assertFalse(reservationFilterView.isFilterOnReservationEndOnly());
    }
    finally {
      DateTimeUtils.setCurrentMillisSystem();
    }
  }

  @Test
  public void testElapsedPeriod() {
    DateTime now = DateTime.now();

    try {
      DateTimeUtils.setCurrentMillisFixed(now.toDateTime().getMillis());
      reservationFilterView = new ReservationFilterView("testId", "testLabel", Months.THREE, true);

      assertThat(reservationFilterView.getStart(), is(now.minus(Months.THREE)));
      assertThat(reservationFilterView.getEnd(), is(now));
      assertThat(Lists.newArrayList(reservationFilterView.getStates()), hasItems(ReservationStatus.values()));
      assertFalse(reservationFilterView.isFilterOnReservationEndOnly());
    }
    finally {
      DateTimeUtils.setCurrentMillisSystem();
    }
  }

  @Test
  public void testActive() {
    DateTime now = DateTime.now();

    try {
      DateTimeUtils.setCurrentMillisFixed(now.toDateTime().getMillis());
      reservationFilterView = new ReservationFilterView("testId", "testLabel", ReservationStatus.RUNNING);

      assertThat(reservationFilterView.getStart(), nullValue());
      assertThat(reservationFilterView.getEnd(), nullValue());
      assertThat(Lists.newArrayList(reservationFilterView.getStates()), hasItem(ReservationStatus.RUNNING));
      assertFalse(reservationFilterView.isFilterOnReservationEndOnly());
    }
    finally {
      DateTimeUtils.setCurrentMillisSystem();
    }
  }

}
