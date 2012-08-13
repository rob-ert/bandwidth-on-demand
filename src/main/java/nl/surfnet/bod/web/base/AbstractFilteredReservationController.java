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
package nl.surfnet.bod.web.base;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.Security;
import nl.surfnet.bod.web.view.ReservationFilterView;
import nl.surfnet.bod.web.view.ReservationView;

import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.Lists;

import static nl.surfnet.bod.web.WebUtils.FILTER_LIST;
import static nl.surfnet.bod.web.WebUtils.FILTER_SELECT;
import static nl.surfnet.bod.web.WebUtils.MAX_ITEMS_PER_PAGE;
import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
import static nl.surfnet.bod.web.WebUtils.calculateFirstPage;
import static nl.surfnet.bod.web.WebUtils.calculateMaxPages;

/**
 * Base controller for filtering and sorting {@link Reservation}s.
 * 
 * @see AbstractSortableListController
 * 
 */
public abstract class AbstractFilteredReservationController extends
    AbstractSearchableSortableListController<ReservationView, Reservation> {
  public static final String FILTER_URL = "filter/";

  private static final String DEFAULT_FILTER_ID = ReservationFilterViewFactory.COMING;

  @Resource
  private ReservationService reservationService;

  @Resource
  private ReservationFilterViewFactory reservationFilterViewFactory;

  @Override
  public String getDefaultSortProperty() {
    return "name";
  }

  /**
   * Selects a default filter when no filter is selected yet, never show all
   * reservations at once....
   */
  @Override
  public String list(Integer page, String sort, String order, Model model) {
    String filterName = WebUtils.getAttributeFromModel(FILTER_SELECT, model);

    if (!StringUtils.hasText(filterName)) {
      filterName = DEFAULT_FILTER_ID;
    }

    return filter(page, sort, order, "", filterName, model);
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
  @RequestMapping(value = FILTER_URL + "{filterId}", method = RequestMethod.GET)
  public String filter(@RequestParam(value = PAGE_KEY, required = false) Integer page,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "order", required = false) String order,//
      @RequestParam(value = "search", required = false) String search, //
      @PathVariable(value = "filterId") String filterId, //
      Model model) {

    List<ReservationView> result = new ArrayList<>();
    Sort sortOptions = prepareSortOptions(sort, order, model);
    ReservationFilterView reservationFilter = reservationFilterViewFactory.create(filterId);
    model.addAttribute(FILTER_SELECT, reservationFilter);

    List<ReservationView> filterList = list(calculateFirstPage(page), MAX_ITEMS_PER_PAGE, sortOptions, model);

    if (StringUtils.hasText(search)) {
      model.addAttribute(WebUtils.PARAM_SEARCH, search);

      result = getFullTextSearchableService().searchForInFilteredList(getEntityClass(), search,
          calculateFirstPage(page), MAX_ITEMS_PER_PAGE, sortOptions, Security.getUserDetails(), filterList);

      model.addAttribute(WebUtils.MAX_PAGES_KEY, calculateMaxPages(getFullTextSearchableService()
          .countSearchForInFilteredList(getEntityClass(), search, filterList)));
    }
    else {
      result = filterList;
      model.addAttribute(WebUtils.MAX_PAGES_KEY, calculateMaxPages(result.size()));
    }

    model.addAttribute(WebUtils.DATA_LIST, result);
    return listUrl();
  }

  @Override
  protected long count() {
    throw new UnsupportedOperationException("Only filtered lists are supported");
  }

  @ModelAttribute
  protected void populateFilter(Model model) {

    model.addAttribute(FILTER_LIST, determineFilters());

    // Remove the [list] part of the url
    model.addAttribute("baseFilterUrl", StringUtils.delete(listUrl(), WebUtils.LIST) + "/" + FILTER_URL);
  }

  protected ReservationService getReservationService() {
    return reservationService;
  }

  protected ReservationFilterViewFactory getReservationFilterViewFactory() {
    return reservationFilterViewFactory;
  }

  @Override
  protected AbstractFullTextSearchService<ReservationView, Reservation> getFullTextSearchableService() {
    return getReservationService();
  }

  private List<ReservationFilterView> determineFilters() {
    List<ReservationFilterView> filterViews = Lists.newArrayList();

    // Coming period
    filterViews.add(reservationFilterViewFactory.create(nl.surfnet.bod.support.ReservationFilterViewFactory.COMING));

    // Elapsed period
    filterViews.add(reservationFilterViewFactory.create(nl.surfnet.bod.support.ReservationFilterViewFactory.ELAPSED));

    filterViews.add(reservationFilterViewFactory.create(ReservationFilterViewFactory.ACTIVE));

    List<Integer> uniqueReservationYears = reservationService.findUniqueYearsFromReservations();

    filterViews.addAll(reservationFilterViewFactory.create(uniqueReservationYears));

    return filterViews;
  }

}
