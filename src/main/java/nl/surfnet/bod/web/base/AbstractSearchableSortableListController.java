package nl.surfnet.bod.web.base;

import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.Security;

import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.Lists;

import static nl.surfnet.bod.web.WebUtils.MAX_ITEMS_PER_PAGE;
import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
import static nl.surfnet.bod.web.WebUtils.calculateFirstPage;
import static nl.surfnet.bod.web.WebUtils.calculateMaxPages;

/**
 * Base controller which adds full text search functionality to the
 * {@link AbstractSortableListController}
 * 
 * @param <T>
 *          DomainObject
 * @param <K>
 */
public abstract class AbstractSearchableSortableListController<T, K> extends AbstractSortableListController<T> {
  
  @Resource
  private ReservationFilterViewFactory reservationFilterViewFactory;

  @RequestMapping(value = "search", method = RequestMethod.GET)
  public String search(@RequestParam(value = PAGE_KEY, required = false) Integer page,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "order", required = false) String order, //
      @RequestParam(value = "search") String search, //
      Model model) {
    Sort sortOptions = prepareSortOptions(sort, order, model);

    if (StringUtils.hasText(search)) {
      List<K> list = Lists.newArrayList();
      model.addAttribute(WebUtils.PARAM_SEARCH, search);

      list = getFullTextSearchableService().searchFor(getEntityClass(), search, calculateFirstPage(page),
          MAX_ITEMS_PER_PAGE, sortOptions);

      model.addAttribute(WebUtils.MAX_PAGES_KEY,
          calculateMaxPages(getFullTextSearchableService().countSearchFor(getEntityClass(), search)));

      model.addAttribute(WebUtils.DATA_LIST,
          getFullTextSearchableService().transformToView(list, Security.getUserDetails()));

    }
    else {
      model.addAttribute(WebUtils.MAX_PAGES_KEY, calculateMaxPages(count()));
      model.addAttribute(WebUtils.DATA_LIST, list(calculateFirstPage(page), MAX_ITEMS_PER_PAGE, sortOptions, model));
    }

    return listUrl();
  }

  protected abstract Class<K> getEntityClass();

  protected abstract AbstractFullTextSearchService<T, K> getFullTextSearchableService();
}
