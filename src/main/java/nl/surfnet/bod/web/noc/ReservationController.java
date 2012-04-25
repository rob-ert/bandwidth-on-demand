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
package nl.surfnet.bod.web.noc;

import static nl.surfnet.bod.web.WebUtils.FILTER_LIST;
import static nl.surfnet.bod.web.WebUtils.FILTER_SELECT;
import static nl.surfnet.bod.web.WebUtils.LIST;
import static nl.surfnet.bod.web.WebUtils.MAX_ITEMS_PER_PAGE;
import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
import static nl.surfnet.bod.web.WebUtils.calculateFirstPage;

import java.util.List;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.base.AbstractSortableListController;
import nl.surfnet.bod.web.view.ReservationFilterView;
import nl.surfnet.bod.web.view.ReservationView;

import org.joda.time.Hours;
import org.joda.time.ReadablePeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

@RequestMapping(ReservationController.PAGE_URL)
@Controller(value = "nocReservationController")
public class ReservationController extends AbstractSortableListController<ReservationView> {

  public static final ReadablePeriod DEFAULT_RESERVATON_DURATION = Hours.FOUR;
  public static final String FILTER_COMMING_PERIOD = "comming";
  public static final String FILTER_ELAPSED_PERIOD = "elapsed";

  static final String PAGE_URL = "noc/reservations";
  static final String MODEL_KEY = "reservation";

   static final Function<Reservation, ReservationView> TO_RESERVATION_VIEW = new Function<Reservation, ReservationView>() {
    @Override
    public ReservationView apply(Reservation reservation) {
      return new ReservationView(reservation);
    }
  };

  @Autowired
  private ReservationService reservationService;

  @Autowired
  private ReservationFilterViewFactory reservationFilterViewFactory;

  public String getDefaultFilterUrl() {
    return "redirect:/" + PAGE_URL + "/filter/" + ReservationFilterViewFactory.COMMING;
  }

  @Override
  public String listUrl() {
    return PAGE_URL + LIST;
  }

  @Override
  public String defaultSortProperty() {
    return "name";
  }

  @ModelAttribute
  protected void populateFilter(Model model) {

    model.addAttribute(FILTER_LIST, determineFilters());
  }

  private List<ReservationFilterView> determineFilters() {
    List<ReservationFilterView> filterViews = Lists.newArrayList();

    // Coming period
    filterViews.add(reservationFilterViewFactory.create(nl.surfnet.bod.support.ReservationFilterViewFactory.COMMING));

    // Elapsed period
    filterViews.add(reservationFilterViewFactory.create(nl.surfnet.bod.support.ReservationFilterViewFactory.ELAPSED));

    List<Double> uniqueReservationYears = reservationService.findUniqueYearsFromReservations();

    filterViews.addAll(reservationFilterViewFactory.create(uniqueReservationYears));
    return filterViews;
  }

  /**
   * Retrieves a list and filters by applying the filter specified by the
   * filterId. After the user selects a filter a new Http get with the selected
   * filterId can be performed.
   * 
   * @param page
   *          StartPage
   * @param sort
   *          Property names to sort on
   * @param order
   *          Order for the sort
   * @param filterId
   *          Id of the filter to apply
   * @param model
   *          Model to place the state on {@link WebUtils#FILTER_SELECT} and
   *          {@link WebUtils#DATA_LIST}
   * @param request
   * @return
   */
  @RequestMapping(value = "filter/{filterId}", method = RequestMethod.GET)
  public String list(@RequestParam(value = PAGE_KEY, required = false) Integer page,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "order", required = false) String order, @PathVariable(value = "filterId") String filterId,
      Model model) {
    
    ReservationFilterView reservationFilter = reservationFilterViewFactory.create(filterId);
    model.addAttribute(FILTER_SELECT, reservationFilter);

    Sort sortOptions = super.prepareSortOptions(sort, order, model);
    List<ReservationView> reservationViews = list(calculateFirstPage(page), MAX_ITEMS_PER_PAGE, sortOptions, model);

    model.addAttribute(WebUtils.DATA_LIST, reservationViews);

    return listUrl();
  }

  @Override
  protected List<ReservationView> list(int firstPage, int maxItems, Sort sort, Model model) {
    ReservationFilterView filter = WebUtils.getAttributeFromModel(FILTER_SELECT, model);

    model.addAttribute("maxPages", WebUtils.calculateMaxPages(reservationService.countAllEntriesUsingFilter((filter))));

    return Lists.transform(reservationService.findAllEntriesUsingFilter(filter, firstPage, maxItems, sort),
        TO_RESERVATION_VIEW);
  }

  /**
   * Selects a default filter, never show all reservations at once....
   */
  @Override
  public String list(Integer page, String sort, String order, Model model) {
    return getDefaultFilterUrl();
  }

  @Override
  protected long count() {
    throw new UnsupportedOperationException("Only filtered lists are supported");
  }


}
