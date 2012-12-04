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
package nl.surfnet.bod.support;

import java.util.List;

import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.web.view.ReservationFilterView;

import org.joda.time.DurationFieldType;
import org.joda.time.Months;
import org.joda.time.ReadablePeriod;
import org.springframework.stereotype.Component;
import org.springframework.util.NumberUtils;

import com.google.common.collect.Lists;

@Component
public class ReservationFilterViewFactory {

  public static final ReadablePeriod DEFAULT_FILTER_INTERVAL = Months.FOUR;
  public static final String DEFAULT_FILTER_INTERVAL_STRING = DEFAULT_FILTER_INTERVAL.getValue(0) + " "
      + DEFAULT_FILTER_INTERVAL.getFieldType(0);

  public static final String COMING = "coming";
  public static final String ELAPSED = "elapsed";
  public static final String ACTIVE = "active";
  
  public ReservationFilterView create(String id) {
    try {
      // If it is a number we assume it is a year
      Integer year = NumberUtils.parseNumber(id, Integer.class);
      return new ReservationFilterView(year);
    }
    catch (IllegalArgumentException exc) {
      switch (id) {
      case ELAPSED:
        return new ReservationFilterView(ELAPSED, String.format("Past %d months",
            DEFAULT_FILTER_INTERVAL.get(DurationFieldType.months())), DEFAULT_FILTER_INTERVAL, true);
      case COMING:
        return new ReservationFilterView(COMING, String.format("In %d months",
            DEFAULT_FILTER_INTERVAL.get(DurationFieldType.months())), DEFAULT_FILTER_INTERVAL, false);
      case ACTIVE:
        return new ReservationFilterView(ACTIVE, "Active", ReservationStatus.RUNNING);
      default:
        throw new IllegalArgumentException("No filter related to: " + id);
      }
    }
  }

  public List<ReservationFilterView> create(List<Integer> reservationYears) {
    List<ReservationFilterView> filterViews = Lists.newArrayList();

    // Years with reservations
    for (Integer year : reservationYears) {
      filterViews.add(create(year.toString()));
    }

    return filterViews;
  }
}
