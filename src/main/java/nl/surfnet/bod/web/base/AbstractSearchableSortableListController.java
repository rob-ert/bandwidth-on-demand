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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import nl.surfnet.bod.domain.PersistableDomain;
import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.support.ReservationFilterViewFactory;
import nl.surfnet.bod.util.FullTextSearchResult;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.apache.lucene.queryParser.ParseException;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Base controller which adds full text search functionality to the
 * {@link AbstractSortableListController}
 */
public abstract class AbstractSearchableSortableListController<VIEW, ENTITY extends PersistableDomain> {

  @Resource
  private ReservationFilterViewFactory reservationFilterViewFactory;

  @RequestMapping(method = RequestMethod.GET)
  public String list(@RequestParam(value = PAGE_KEY, required = false) Integer page,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "order", required = false) String order,
      Model model) {

    Sort sortOptions = prepareSortOptions(sort, order, model);

    model.addAttribute(WebUtils.MAX_PAGES_KEY, calculateMaxPages(count(model)));
    model.addAttribute(WebUtils.DATA_LIST, list(calculateFirstPage(page), MAX_ITEMS_PER_PAGE, sortOptions, model));

    return listUrl();
  }

  @RequestMapping(value = "search", method = RequestMethod.GET)
  public String search(
      @RequestParam(value = PAGE_KEY, required = false) Integer page,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "order", required = false) String order,
      @RequestParam(value = "search") String search,
      Model model) {

    if (!StringUtils.hasText(search)) {
      return list(page, sort, order, model);
    }

    Sort sortOptions = prepareSortOptions(sort, order, model);
    Integer firstItem = calculateFirstPage(page);
    List<Long> listFromController = handleListFromController(model);

    String translatedSearchString = mapLabelToTechnicalName(search);

    try {
      FullTextSearchResult<ENTITY> searchResult = getFullTextSearchableService().searchForInFilteredList(
          getEntityClass(), translatedSearchString, firstItem, MAX_ITEMS_PER_PAGE, sortOptions,
          Security.getUserDetails(), listFromController);

      model.addAttribute(WebUtils.PARAM_SEARCH, search);
      model.addAttribute(WebUtils.DATA_LIST, transformToView(searchResult.getResultList(), Security.getUserDetails()));
      model.addAttribute(WebUtils.MAX_PAGES_KEY, calculateMaxPages(searchResult.getTotalCount()));

    }
    catch (ParseException e) {
      // Do not search, but show default list
      model.addAttribute(WebUtils.WARN_MESSAGES_KEY,
          Lists.newArrayList("Sorry, we could not process your search query."));
    }

    return listUrl();
  }

  public abstract List<VIEW> transformToView(List<ENTITY> entities, RichUserDetails user);

  protected Sort prepareSortOptions(String sort, String order, Model model) {
    String sortProperty = sortProperty(sort);
    Direction sortDirection = sortDirection(order);

    model.addAttribute("sortProperty", sortProperty);
    model.addAttribute("sortDirection", sortDirection);

    return sortOrder(translateSortProperty(sortProperty), sortDirection);
  }

  protected abstract String listUrl();

  protected abstract List<VIEW> list(int firstPage, int maxItems, Sort sort, Model model);

  protected abstract long count(Model model);

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

  // TODO: Change to optional?
  public abstract List<Long> handleListFromController(Model model);

  /**
   * Maps a search string with a label from a column to a search with a field in
   * the domain model. Can be overridden for a specific implementation, this
   * default implementation maps to the most common used fields.
   *
   * @param search
   *          The string to search for, may contain lucene specific syntax e.g.
   *          'name:test'
   * @return String replaced string
   */
  protected String mapLabelToTechnicalName(String search) {
    search = WebUtils.replaceSearchWith(search, "team:", "virtualResourceGroup.name:");
    search = WebUtils.replaceSearchWith(search, "institute:", "physicalResourceGroup.institute.name:");
    search = WebUtils.replaceSearchWith(search, "physicalPort:", "physicalPort.nmsPortId:");
    return search;
  }

  @SuppressWarnings("unchecked")
  private Class<ENTITY> getEntityClass() {
    return (Class<ENTITY>) resolveParameterizedType(this.getClass(), AbstractSearchableSortableListController.class)
        .getActualTypeArguments()[1];
  }

  private ParameterizedType resolveParameterizedType(Type initialType, Class<?> targetType) {
    if (initialType instanceof ParameterizedType && ((ParameterizedType) initialType).getRawType().equals(targetType)) {
      return (ParameterizedType) initialType;
    }

    Class<?> rawType = (Class<?>) initialType;
    Type superType = rawType.getGenericSuperclass();
    if (superType != null && !superType.equals(Object.class))
      return resolveParameterizedType(superType, targetType);

    throw new IllegalStateException();
  }

  protected abstract AbstractFullTextSearchService<ENTITY> getFullTextSearchableService();
  
  protected List<Long> getOptionalsOrEmptyList(final Optional<List<Long>> ids) {
    if(ids.isPresent()){
      return ids.get();
    }
    // TODO: Change to optional?
    return new ArrayList<>();
  }
}
