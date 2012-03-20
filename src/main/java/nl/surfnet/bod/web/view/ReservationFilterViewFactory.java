package nl.surfnet.bod.web.view;

import java.util.List;

import javax.annotation.PostConstruct;

import nl.surfnet.bod.web.WebUtils;

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

  private static final String COMMING = "comming";
  private static final String ELAPSED = "elapsed";

  @Autowired
  private MessageSource messageSource;

  public ReservationFilterView COMMING_PERIOD_FILTER;

  public ReservationFilterView ELAPSED_PERIOD_FILTER;

  @PostConstruct
  public void init() {
    COMMING_PERIOD_FILTER = new ReservationFilterView(COMMING, WebUtils.getMessage(messageSource,
        "label_reservation_filter_comming_period", DEFAULT_FILTER_INTERVAL.get(DurationFieldType.months())),
        DEFAULT_FILTER_INTERVAL, false);

    ELAPSED_PERIOD_FILTER = new ReservationFilterView(ELAPSED, WebUtils.getMessage(messageSource,
        "label_reservation_filter_elapsed_period", DEFAULT_FILTER_INTERVAL.get(DurationFieldType.months())),
        DEFAULT_FILTER_INTERVAL, true);
  }

  public ReservationFilterView get(String id) {

    try {
      Integer year = NumberUtils.parseNumber(id, Integer.class);
      return new ReservationFilterView(year);
    }
    catch (IllegalArgumentException exc) {

      if (COMMING.equals(id)) {
        return COMMING_PERIOD_FILTER;
      }
      else if (ELAPSED.equals(id)) {
        return ELAPSED_PERIOD_FILTER;
      }
      else
        throw new IllegalArgumentException("No filter available for id: " + id);
    }
  }

  public List<ReservationFilterView> create(List<Double> reservationYears) {
    List<ReservationFilterView> filterViews = Lists.newArrayList();

    // Years with reservations
    for (Double year : reservationYears) {
      filterViews.add(get(year.toString()));
    }

    return filterViews;
  }
}
