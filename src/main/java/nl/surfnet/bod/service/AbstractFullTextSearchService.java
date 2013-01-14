/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import nl.surfnet.bod.domain.PersistableDomain;
import nl.surfnet.bod.util.FullTextSearchContext;
import nl.surfnet.bod.util.FullTextSearchResult;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.apache.lucene.queryParser.ParseException;
import org.springframework.data.domain.Sort;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

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
  private List<ENTITY> searchFor(Class<ENTITY> entityClass, String searchText) throws ParseException {
    FullTextSearchContext<ENTITY> fullTextSearchContext = new FullTextSearchContext<ENTITY>(getEntityManager(),
        entityClass);

    return fullTextSearchContext.getFullTextQueryForKeywordOnAllAnnotedFields(searchText).getResultList();
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
      int firstResult, int maxResults, RichUserDetails userDetails, List<Long> filterResult) throws ParseException {

    Preconditions.checkArgument(firstResult >= 0);

    List<ENTITY> searchResult = searchFor(entityClass, searchText);

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
   * empty, no elements are found.
   * 
   * <Strong> The items will be sorted according to the sequence of the
   * filterResults. </Strong>
   * 
   * @param searchResults
   *          The unsorted search results
   * @param filterResults
   *          Id's of the items that are allowed to be shown, the found elements
   *          remain sorted the same way these filterResults are.
   */
  @VisibleForTesting
  protected List<ENTITY> intersectFullTextResultAndFilterResult(List<ENTITY> searchResults,
      final List<Long> filterResults) {

    final List<ENTITY> sortedEntityList = new ArrayList<>();
    for (final Long id : filterResults) {
      Optional<ENTITY> foundEntity = Iterables.tryFind(searchResults, new Predicate<ENTITY>() {
        @Override
        public boolean apply(ENTITY entity) {
          return id.equals(entity.getId());
        }
      });

      if (foundEntity.isPresent()) {
        sortedEntityList.add(foundEntity.get());
      }

    }
    return sortedEntityList;
  }
}