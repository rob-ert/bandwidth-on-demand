package nl.surfnet.bod.web.base;

import java.util.List;

import nl.surfnet.bod.service.FullTextSearchableService;
import nl.surfnet.bod.web.WebUtils;

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
 */
public abstract class AbstractSearchableSortableListController<T> extends AbstractSortableListController<T> {

  @RequestMapping(value = "search", method = RequestMethod.GET)
  public String search(@RequestParam(value = PAGE_KEY, required = false) Integer page,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "order", required = false) String order, @RequestParam String text, Model model) {
    List<T> list = Lists.newArrayList();

    Sort sortOptions = prepareSortOptions(sort, order, model);
    if (StringUtils.hasText(text)) {
      list = getFullTextSearchableService().searchFor(text, calculateFirstPage(page), MAX_ITEMS_PER_PAGE, sortOptions);
    }

    model.addAttribute("maxPages", calculateMaxPages(getFullTextSearchableService().countSearchFor(text)));
    model.addAttribute("text", text);
    model.addAttribute(WebUtils.DATA_LIST, list);

    return listUrl();
  }

  protected abstract FullTextSearchableService<T> getFullTextSearchableService();

}
