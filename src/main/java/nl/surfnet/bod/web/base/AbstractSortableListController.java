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

import nl.surfnet.bod.web.WebUtils;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

/**
 * Base controller class which facilitates sorting
 *
 */
public abstract class AbstractSortableListController<VIEW> {

  @RequestMapping(method = RequestMethod.GET)
  public String list(@RequestParam(value = PAGE_KEY, required = false) Integer page,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "order", required = false) String order,
      Model model) {

    Sort sortOptions = prepareSortOptions(sort, order, model);

    model.addAttribute(WebUtils.MAX_PAGES_KEY, calculateMaxPages(count()));
    model.addAttribute(WebUtils.DATA_LIST, list(calculateFirstPage(page), MAX_ITEMS_PER_PAGE, sortOptions, model));

    return listUrl();
  }

  protected Sort prepareSortOptions(String sort, String order, Model model) {
    String sortProperty = sortProperty(sort);
    Direction sortDirection = sortDirection(order);

    model.addAttribute("sortProperty", sortProperty);
    model.addAttribute("sortDirection", sortDirection);

    return sortOrder(translateSortProperty(sortProperty), sortDirection);
  }

  protected abstract String listUrl();

  protected abstract List<VIEW> list(int firstPage, int maxItems, Sort sort, Model model);

  protected abstract long count();

  protected String getDefaultSortProperty() {
    return "id";
  }

  protected Direction getDefaultSortOrder() {
    return Direction.ASC;
  }

  protected List<String> translateSortProperty(String sortProperty) {
    return ImmutableList.of(sortProperty);
  }

  protected Sort sortOrder(List<String> sortProperties, Direction direction) {
    return sort(direction, sortProperties);
  }

  private String sortProperty(String order) {
    if (Strings.emptyToNull(order) == null || !doesPropertyExist(order)) {
      return getDefaultSortProperty();
    }

    return order;
  }

  private boolean doesPropertyExist(String order) {
    try {
      ParameterizedType type;
      if (getClass().getGenericSuperclass() instanceof ParameterizedType) {
        type = (ParameterizedType) getClass().getGenericSuperclass();
      }
      else {
        type = (ParameterizedType) getClass().getSuperclass().getGenericSuperclass();
      }

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
      return getDefaultSortOrder();
    }

    try {
      return Direction.fromString(order);
    }
    catch (IllegalArgumentException e) {
      return getDefaultSortOrder();
    }
  }

  private Sort sort(final Direction direction, List<String> properties) {
    return new Sort(direction, properties);
  }
}
