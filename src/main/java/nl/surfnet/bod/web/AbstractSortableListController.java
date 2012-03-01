package nl.surfnet.bod.web;

import static nl.surfnet.bod.web.WebUtils.MAX_ITEMS_PER_PAGE;
import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
import static nl.surfnet.bod.web.WebUtils.calculateFirstPage;
import static nl.surfnet.bod.web.WebUtils.calculateMaxPages;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

public abstract class AbstractSortableListController<T> {

  public static final Direction DEFAULT_SORT_DIRECTION = Direction.ASC;

  @RequestMapping(method = RequestMethod.GET)
  public String list(@RequestParam(value = PAGE_KEY, required = false) Integer page,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "order", required = false) String order, Model model) {

    String sortProperty = sortProperty(sort);
    Direction sortDirection = sortDirection(order);

    model.addAttribute("sortProperty", sortProperty);
    model.addAttribute("sortDirection", sortDirection);
    model.addAttribute("list",
        list(calculateFirstPage(page), MAX_ITEMS_PER_PAGE, sortOrder(sortProperty, sortDirection)));
    model.addAttribute("maxPages", calculateMaxPages(count()));

    return listUrl();
  }

  protected abstract String listUrl();

  protected abstract List<T> list(int firstPage, int maxItems, Sort sort);

  protected abstract long count();

  protected String defaultSortProperty() {
    return "id";
  }

  protected List<String> translateSortProperty(String sortProperty) {
    return ImmutableList.of(sortProperty);
  }

  protected Sort sortOrder(String sortProperty, Direction direction) {
    return sort(direction, translateSortProperty(sortProperty));
  }

  private String sortProperty(String order) {
    if (Strings.emptyToNull(order) == null || !doesPropertyExist(order)) {
      return defaultSortProperty();
    }

    return order;
  }

  private boolean doesPropertyExist(String order) {
    try {
      ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
      BeanInfo beanInfo = Introspector.getBeanInfo(((Class<?>) type.getActualTypeArguments()[0]));

      for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
        if (property.getName().equals(order)) {
          return true;
        }
      }
    }
    catch (IntrospectionException e) {
      return false;
    }

    return false;
  }

  private Direction sortDirection(String order) {
    if (Strings.isNullOrEmpty(order)) {
      return DEFAULT_SORT_DIRECTION;
    }

    try {
      return Direction.fromString(order);
    }
    catch (IllegalArgumentException e) {
      return DEFAULT_SORT_DIRECTION;
    }
  }

  private Sort sort(final Direction direction, List<String> properties) {
    return new Sort(direction, properties);
  }
}
