package nl.surfnet.bod.web;

import static nl.surfnet.bod.web.WebUtils.MAX_ITEMS_PER_PAGE;
import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
import static nl.surfnet.bod.web.WebUtils.calculateFirstPage;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

public abstract class AbstractFilteredSortableListController<T> extends AbstractSortableListController<T> {
  

  @RequestMapping(value = "/filter/{id}", method = RequestMethod.GET)
  public String list(@RequestParam(value = PAGE_KEY, required = false) Integer page,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "order", required = false) String order, 
      @PathVariable(value="id") Long filterId,
      Model model) {

    super.list(page, sort, order, model);
    
    //Add filterId to model, so a ui component can determine which item is selected
    model.addAttribute("filterId", filterId);
    model.addAttribute("list",
        list(calculateFirstPage(page), MAX_ITEMS_PER_PAGE, sortOrder((String) model.asMap().get("sortProperty" ), (Direction) model.asMap().get("sortOrder")), filterId));

    return listUrl();
  }
  
  protected abstract List<T> list(int firstPage, int maxItems, Sort sort, Long filterId);

  @Override
  protected List<T> list(int firstPage, int maxItems, Sort sort){    
    return list(firstPage, maxItems,sort,null);
  }
  
}
