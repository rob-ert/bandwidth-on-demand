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

import static nl.surfnet.bod.web.WebUtils.*;

import java.util.List;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.view.ReservationFilterView;
import nl.surfnet.bod.web.view.ReservationView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Base controller for filtering and sorting {@link Reservation}s.
 *
 * @see AbstractSortableListController
 *
 * @author Franky
 *
 */
public abstract class AbstractFilteredReservationController extends AbstractSortableListController<ReservationView> {
  public static final String FILTER_URL = "filter/";

  private static final String DEFAULT_FILTER_ID = ReservationFilterViewFactory.COMING;

  @Autowired
  private ReservationService reservationService;

  @Autowired
  private ReservationFilterViewFactory reservationFilterViewFactory;

  @Override
  public String getDefaultSortProperty() {
    return "name";
  }

  protected ReservationService getReservationService() {
    return reservationService;
  }

  protected ReservationFilterViewFactory getReservationFilterViewFactory() {
    return reservationFilterViewFactory;
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

    return list(page, sort, order, filterName, model);
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
  public String list(@RequestParam(value = PAGE_KEY, required = false) Integer page,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "order", required = false) String order, @PathVariable(value = "filterId") String filterId,
      Model model) {

    ReservationFilterView reservationFilter = reservationFilterViewFactory.create(filterId);
    model.addAttribute(FILTER_SELECT, reservationFilter);

    Sort sortOptions = prepareSortOptions(sort, order, model);
    List<ReservationView> reservationViews = list(calculateFirstPage(page), MAX_ITEMS_PER_PAGE, sortOptions, model);

    model.addAttribute(WebUtils.DATA_LIST, reservationViews);

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

  /**
   * Transforms the given reservations to {@link ReservationView}s and
   * determines if the reservation is allowed to be delete by the given user.
   *
   * @param reservationsToTransform
   *          {@link Reservation}s to be transformed
   * @param user
   *          {@link RichUserDetails} to check if this user is allowed to delete
   *          the reservation
   * @return {@link List<ReservationView>} transformed reservations
   */
  public List<ReservationView> transformReservationToReservationView(List<Reservation> reservationsToTransform,
      final RichUserDetails user) {

    return Lists.transform(reservationsToTransform, new Function<Reservation, ReservationView>() {
      @Override
      public ReservationView apply(Reservation reservation) {
        return new ReservationView(reservation, reservationService.isDeleteAllowed(reservation, user.getSelectedRole()));
      }
    });
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
