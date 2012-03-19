package nl.surfnet.bod.web;

import static nl.surfnet.bod.web.WebUtils.MAX_ITEMS_PER_PAGE;
import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
import static nl.surfnet.bod.web.WebUtils.calculateFirstPage;
import static nl.surfnet.bod.web.WebUtils.calculateMaxPages;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Base controller class which facilitates filtering and sorting, upon the
 * {@link AbstractSortableListController}
 *
 * @see AbstractSortableListController
 */
public abstract class AbstractFilteredSortableListController<T> extends AbstractSortableListController<T> {

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
   *          Model to place the state on {@link WebUtils#FILTER_KEY} and
   *          {@link WebUtils#DATA_LIST}
   * @return
   */
  @RequestMapping(value = "/filter/{filterId}", method = RequestMethod.GET)
  public String list(@RequestParam(value = PAGE_KEY, required = false) Integer page,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "order", required = false) String order,
      @PathVariable(value = "filterId") Long filterId,
      Model model) {

    Sort sortOptions = super.prepareSortOptions(sort, order, model);
    model.addAttribute("maxPages", calculateMaxPages(count(filterId, model)));

    // Add filterId to model, so a ui component can determine which item is selected
    model.addAttribute(WebUtils.FILTER_KEY, filterId);

    List<T> list = list(calculateFirstPage(page), MAX_ITEMS_PER_PAGE, sortOptions, filterId, model);
    model.addAttribute(WebUtils.DATA_LIST, list);

    return listUrl();
  }

  /**
   * Retrieves a list of data to be presented, paging is supported. Applies the
   * specified filter to the data and places it on the model using key
   * {@link WebUtils#DATA_LIST}
   *
   * @param firstPage
   *          StartPage
   * @param maxItems
   *          Max amount of items to retrieve
   * @param sort
   *          {@link Sort} options
   * @param filterId
   *          Id of the filter to apply
   * @param model
   *          Model to place the result on
   * @return
   */
  protected abstract List<T> list(int firstPage, int maxItems, Sort sort, Long filterId, Model model);

  /**
   * Determines the amount of items, after applying the specificied filter.
   *
   * @param filterId
   *          Id of the filter to use
   * @param model
   *          Model to retrieve the data from, assumes it is available under key
   *          {@link WebUtils#DATA_LIST}
   * @return long amount of items
   */
  protected abstract long count(Long filterId, Model model);

  /**
   * Dynamically determines filters based on the given data, which will be
   * available for the user to make a selection.
   *
   * @param list
   *          List containing the data
   * @param model
   *          Model filters will be placed on the model using key
   *          {@link WebUtils#FILTER_SELECT}
   * @return Default selectedFilter
   */
  protected abstract void populateFilter(List<T> list, Model model);

  /**
   * Retrieves a list of data to be presented, only used for compatibility
   * reasons, when no filter is available yet.
   *
   * @see #list(Integer, String, String, Model)
   */
  @Override
  protected List<T> list(int firstPage, int maxItems, Sort sort, Model model) {
    return list(firstPage, maxItems, sort, null, model);
  }

}
