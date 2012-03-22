package nl.surfnet.bod.support;

import java.util.List;

import nl.surfnet.bod.web.view.ReservationFilterView;

import org.joda.time.DurationFieldType;
import org.joda.time.Months;
import org.joda.time.ReadablePeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.NumberUtils;

import com.google.common.collect.Lists;

@Component
public class ReservationFilterViewFactory {

  public static final ReadablePeriod DEFAULT_FILTER_INTERVAL = Months.FOUR;

  public static final String COMMING = "comming";
  public static final String ELAPSED = "elapsed";

  public ReservationFilterView create(String id) {

    try {
      // If it is a number we assume it is a year
      Integer year = NumberUtils.parseNumber(id, Integer.class);
      return new ReservationFilterView(year);
    }
    catch (IllegalArgumentException exc) {
      
      if (ELAPSED.equals(id)) {
        return new ReservationFilterView(ELAPSED, String.format("Now - %d months",
            DEFAULT_FILTER_INTERVAL.get(DurationFieldType.months())), DEFAULT_FILTER_INTERVAL, true);
      } else {        
          return new ReservationFilterView(COMMING, String.format("Now + %d months",
              DEFAULT_FILTER_INTERVAL.get(DurationFieldType.months())), DEFAULT_FILTER_INTERVAL, false);
        }
      }
  }

  public List<ReservationFilterView> create(List<Double> reservationYears) {
    List<ReservationFilterView> filterViews = Lists.newArrayList();

    // Years with reservations
    for (Double year : reservationYears) {
      filterViews.add(create(String.valueOf((year.intValue()))));
    }

    return filterViews;
  }
}
