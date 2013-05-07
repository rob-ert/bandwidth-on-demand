/**
 * Copyright (c) 2012, SURFnet BV
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
package nl.surfnet.bod.web.base;

import static nl.surfnet.bod.web.WebUtils.FILTER_LIST;
import static nl.surfnet.bod.web.WebUtils.FILTER_SELECT;
import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;

import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.view.ReservationFilterView;
import nl.surfnet.bod.web.view.ReservationView;

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
 */
public abstract class AbstractFilteredReservationController extends
    AbstractSearchableSortableListController<ReservationView, Reservation> {
  public static final String FILTER_URL = "filter/";

  @Resource
  private ReservationService reservationService;

  @Resource
  private ReservationFilterViewFactory reservationFilterViewFactory;

  @Override
  public String list(Integer page, String sort, String order, Model model) {
    return filter(page, sort, order, ReservationFilterViewFactory.COMING, model);
  }

  @Override
  public String search(Integer page, String sort, String order, String search, Model model) {
    return filterAndSearch(page, sort, order, search, ReservationFilterViewFactory.COMING, model);
  }

  @RequestMapping(value = FILTER_URL + "{filterId}/search", method = RequestMethod.GET)
  public String filterAndSearch(
      @RequestParam(value = PAGE_KEY, required = false) Integer page,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "order", required = false) String order,
      @RequestParam(value = "search", required = false) String search,
      @PathVariable(value = "filterId") String filterId,
      Model model) {

    try {
      ReservationFilterView reservationFilter = reservationFilterViewFactory.create(filterId);
      model.addAttribute(FILTER_SELECT, reservationFilter);

      return super.search(page, sort, order, search, model);
    }
    catch (IllegalArgumentException e) {
      model.asMap().clear();
      return "redirect:../";
    }
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
   * @param filterId
   *          Id of the filter to apply. Nullable, will default to
   *          {@link ReservationFilterViewFactory#DEFAULT_FILTER} when empty or
   *          null.
   * @param model
   *          Model to place the state on {@link WebUtils#FILTER_SELECT} and
   *          {@link WebUtils#DATA_LIST}
   */
  @RequestMapping(value = FILTER_URL + "{filterId}", method = RequestMethod.GET)
  public String filter(
      @RequestParam(value = PAGE_KEY, required = false) Integer page,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "order", required = false) String order,
      @PathVariable(value = "filterId") String filterId,
      Model model) {

    try {
      ReservationFilterView reservationFilter = reservationFilterViewFactory.create(filterId);
      model.addAttribute(FILTER_SELECT, reservationFilter);

      return super.list(page, sort, order, model);
    }
    catch (IllegalArgumentException e) {
      model.asMap().clear();
      return "redirect:../";
    }
  }

  @ModelAttribute
  protected void populateFilter(Model model) {
    model.addAttribute(FILTER_LIST, determineFilters());

    // Remove the [list] part of the url
    model.addAttribute("baseFilterUrl", StringUtils.delete(listUrl(), WebUtils.LIST) + "/" + FILTER_URL);
  }

  @Override
  public String getDefaultSortProperty() {
    return "startDateTime";
  }

  protected ReservationService getReservationService() {
    return reservationService;
  }

  protected ReservationFilterViewFactory getReservationFilterViewFactory() {
    return reservationFilterViewFactory;
  }

  @Override
  protected AbstractFullTextSearchService<Reservation> getFullTextSearchableService() {
    return getReservationService();
  }

  private List<ReservationFilterView> determineFilters() {
    List<ReservationFilterView> filterViews = Lists.newArrayList(
      reservationFilterViewFactory.create(nl.surfnet.bod.support.ReservationFilterViewFactory.COMING),
      reservationFilterViewFactory.create(nl.surfnet.bod.support.ReservationFilterViewFactory.ELAPSED),
      reservationFilterViewFactory.create(ReservationFilterViewFactory.ACTIVE));

    List<Integer> uniqueReservationYears = reservationService.findUniqueYearsFromReservations();
    filterViews.addAll(reservationFilterViewFactory.create(uniqueReservationYears));

    return filterViews;
  }

  @Override
  protected List<? extends ReservationView> transformToView(List<? extends Reservation> reservationsToTransform, final RichUserDetails user) {

    return Lists.transform(reservationsToTransform, new Function<Reservation, ReservationView>() {
      @Override
      public ReservationView apply(Reservation reservation) {
        return new ReservationView(
          reservation,
          reservationService.isDeleteAllowed(reservation, user),
          reservationService.isEditAllowed(reservation, user.getSelectedRole()));
      }
    });
  }

}
