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
 * Base controller class which facilitates filtering and sorting, based on the
 * {@link AbstractSortableListController}
 * 
 * @see AbstractSortableListController
 */
public abstract class AbstractFilteredSortableListController<T> extends AbstractSortableListController<T> {

  @RequestMapping(value = "/filter/{id}", method = RequestMethod.GET)
  public String list(@RequestParam(value = PAGE_KEY, required = false) Integer page,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "order", required = false) String order, @PathVariable(value = "id") Long filterId,
      Model model) {

    Sort sortOptions = super.prepareSortOptions(sort, order, model);
    model.addAttribute("maxPages", calculateMaxPages(count(filterId)));

    // Add filterId to model, so a ui component can determine which item is
    // selected
    model.addAttribute("filterId", filterId);
    model.addAttribute("list", list(calculateFirstPage(page), MAX_ITEMS_PER_PAGE, sortOptions, filterId));

    return listUrl();
  }

  protected abstract long count(Long filterId);

  protected abstract List<T> list(int firstPage, int maxItems, Sort sort, Long filterId);

  @Override
  protected List<T> list(int firstPage, int maxItems, Sort sort) {
    return list(firstPage, maxItems, sort, null);
  }

}
