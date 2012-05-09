package nl.surfnet.bod.web.base;

import static nl.surfnet.bod.web.WebUtils.*;

import java.util.List;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.web.WebUtils;
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
  private static final String DEFAULT_FILTER_ID = ReservationFilterViewFactory.COMING;

  private static final Function<Reservation, ReservationView> TO_RESERVATION_VIEW = new Function<Reservation, ReservationView>() {
    @Override
    public ReservationView apply(Reservation reservation) {
      return new ReservationView(reservation);
    }
  };

  private static final String FILTER_URL = "filter/";

  @Autowired
  protected ReservationService reservationService;

  @Autowired
  protected ReservationFilterViewFactory reservationFilterViewFactory;

  @Override
  public String defaultSortProperty() {
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

    Sort sortOptions = super.prepareSortOptions(sort, order, model);
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

    //Remove the [list] part of the url
    model.addAttribute("baseFilterUrl", StringUtils.delete(listUrl(), WebUtils.LIST) + "/" + FILTER_URL);
  }

  // TODO make protected
  public List<ReservationView> transformReservationToReservationView(List<Reservation> reservationsToTransform) {
    return Lists.transform(reservationsToTransform, TO_RESERVATION_VIEW);
  }

  private List<ReservationFilterView> determineFilters() {
    List<ReservationFilterView> filterViews = Lists.newArrayList();

    // Coming period
    filterViews.add(reservationFilterViewFactory.create(nl.surfnet.bod.support.ReservationFilterViewFactory.COMING));

    // Elapsed period
    filterViews.add(reservationFilterViewFactory.create(nl.surfnet.bod.support.ReservationFilterViewFactory.ELAPSED));

    List<Double> uniqueReservationYears = reservationService.findUniqueYearsFromReservations();

    filterViews.addAll(reservationFilterViewFactory.create(uniqueReservationYears));

    return filterViews;
  }

}
