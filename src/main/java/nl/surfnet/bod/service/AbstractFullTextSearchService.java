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
package nl.surfnet.bod.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import nl.surfnet.bod.util.FullTextSearchContext;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.hibernate.search.jpa.FullTextQuery;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import com.google.common.annotations.VisibleForTesting;

/**
 * Service interface to abstract full text search functionality
 * 
 * @param <K>
 *          DomainObject
 */
public abstract class AbstractFullTextSearchService<T, K> {

  /**
   * Performs a full text search on the given searchText.
   * 
   * @param searchText
   *          String text to search for
   * @param firstResult
   *          int startItem
   * @param maxResults
   *          int max amount of items
   * @param sort
   *          {@link Sort} sorting options
   * 
   * @return List<K> result list
   */
  @SuppressWarnings("unchecked")
  public List<K> searchFor(Class<K> entityClass, String searchText, int firstResult, int maxResults, Sort sort) {
    FullTextQuery jpaQuery = createSearchQuery(searchText, sort, entityClass);

    // Limit for paging
    jpaQuery.setFirstResult(firstResult);
    jpaQuery.setMaxResults(maxResults);

    return jpaQuery.getResultList();
  }

  /**
   * Performs a full text search on the given searchText and combines it with
   * the specified filteredItems. The intersection of both lists will be
   * returned.
   * 
   * @param searchText
   *          String text to search for
   * @param firstResult
   *          int startItem
   * @param maxResults
   *          int max amount of items
   * @param sort
   *          {@link Sort} sorting options
   * @param filteredItems
   *          list of already found items
   * 
   * @return List<K> result list
   */
  public List<T> searchForInFilteredList(Class<K> entityClass, String searchText, int firstResult, int maxResults,
      Sort sort, RichUserDetails userDetails, List<T> filterResult) {

    List<T> searchResult = transformToView(searchFor(entityClass, searchText, firstResult, maxResults, sort),
        userDetails);

    List<T> intersectedList = intersectFullTextResultAndFilterResult(searchResult, filterResult);

    // limit to size of list
    int toSize = Math.min(firstResult + maxResults, firstResult + intersectedList.size());
    toSize = Math.min(toSize, intersectedList.size());
    return intersectedList.subList(firstResult, toSize);
  }

  public long countSearchFor(Class<K> entityClass, String searchText) {
    FullTextQuery jpaQuery = createSearchQuery(searchText, null, entityClass);

    return jpaQuery.getResultList().size();
  }

  @SuppressWarnings("unchecked")
  public long countSearchForInFilteredList(Class<K> entityClass, String searchText, List<T> filteredItems) {
    FullTextQuery jpaQuery = createSearchQuery(searchText, entityClass);

    return intersectFullTextResultAndFilterResult(filteredItems, new ArrayList<T>(jpaQuery.getResultList())).size();
  }

  /**
   * Transforms the given list to the corresponding view and applies some user
   * specific restrictions if appropiate.
   * 
   * @param List
   *          <K> listToTransform list to be transformed
   * @param user
   *          {@link RichUserDetails} to check user specific restrictions
   * 
   * @return {@link List<T>} transformed reservations
   */
  public abstract List<T> transformToView(List<K> listToTransform, final RichUserDetails user);

  protected abstract EntityManager getEntityManager();

  private FullTextQuery createSearchQuery(String searchText, Class<K> entityClass) {
    return createSearchQuery(searchText, null, entityClass);
  }

  private FullTextQuery createSearchQuery(String searchText, Sort sort, Class<K> entityClass) {
    FullTextSearchContext<K> fullTextSearchContext = new FullTextSearchContext<K>(getEntityManager(), entityClass);

    return fullTextSearchContext.getFullTextQueryForKeywordOnAllAnnotedFields(searchText, sort);
  }

  /**
   * FInds objects that both list have in common. When one or both lists are
   * empty, no elements are found
   * 
   * @param searchResults
   *          List<K> List with result of filter
   * @param filterResults
   *          List<K> List with result of search
   * @return List<K> List with common objects
   */
  @VisibleForTesting
  List<T> intersectFullTextResultAndFilterResult(List<T> searchResults, List<T> filterResults) {
    // Prevent modification of the searchResults
    ArrayList<T> intersectedList = new ArrayList<T>(searchResults);

    if (!CollectionUtils.isEmpty(intersectedList)) {
      intersectedList.retainAll(filterResults);
    }

    return intersectedList;
  }

}