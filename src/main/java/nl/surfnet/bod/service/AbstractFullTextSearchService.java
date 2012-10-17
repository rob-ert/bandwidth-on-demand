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

import java.util.List;

import javax.persistence.EntityManager;

import nl.surfnet.bod.domain.PersistableDomain;
import nl.surfnet.bod.util.FullTextSearchContext;
import nl.surfnet.bod.util.FullTextSearchResult;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.apache.lucene.queryParser.ParseException;
import org.springframework.data.domain.Sort;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

/**
 * Service interface to abstract full text search functionality
 *
 * @param <ENTITY>
 *          DomainObject
 */
public abstract class AbstractFullTextSearchService<ENTITY extends PersistableDomain> {

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
   * @return List<ENTITY> result list
   * @throws ParseException
   */
  @SuppressWarnings("unchecked")
  private List<ENTITY> searchFor(Class<ENTITY> entityClass, String searchText, Sort sort) throws ParseException {
    FullTextSearchContext<ENTITY> fullTextSearchContext = new FullTextSearchContext<ENTITY>(getEntityManager(),
        entityClass);

    return fullTextSearchContext.getFullTextQueryForKeywordOnAllAnnotedFields(searchText, sort).getResultList();
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
   * @return List<ENTITY> result list
   * @throws ParseException
   */
  public FullTextSearchResult<ENTITY> searchForInFilteredList(Class<ENTITY> entityClass, String searchText,
      int firstResult, int maxResults, Sort sort, RichUserDetails userDetails, List<Long> filterResult)
      throws ParseException {

    Preconditions.checkArgument(firstResult >= 0);

    List<ENTITY> searchResult = searchFor(entityClass, searchText, sort);

    List<ENTITY> intersectedList = intersectFullTextResultAndFilterResult(searchResult, filterResult);

    return new FullTextSearchResult<>(intersectedList.size(), pageList(firstResult, maxResults, intersectedList));
  }

  /**
   *
   * @param firstResult
   *          firstResult in page
   * @param maxResults
   *          Max. number of results, can be limited due to size of list
   * @param listToPage
   *          List to page
   * @return {@link FullTextSearchResult} containing one page of data
   */
  public List<ENTITY> pageList(int firstResult, int maxResults, List<ENTITY> listToPage) {
    // Determine count and chop list in to page
    int intersectedSize = listToPage.size();
    int lastResult = Math.min(firstResult + maxResults, intersectedSize);
    // FirstResult may not be bigger then list
    if (firstResult > lastResult) {
      firstResult = lastResult;
    }
    return listToPage.subList(firstResult, lastResult);
  }

  protected abstract EntityManager getEntityManager();

  /**
   * FInds objects that both list have in common. When one or both lists are
   * empty, no elements are found
   */
  @VisibleForTesting
  List<ENTITY> intersectFullTextResultAndFilterResult(List<ENTITY> searchResults, final List<Long> filterResults) {
    return FluentIterable.from(searchResults).filter(new Predicate<PersistableDomain>() {
      @Override
      public boolean apply(PersistableDomain domain) {
        return filterResults.contains(domain.getId());
      }
    }).toImmutableList();
  }

}