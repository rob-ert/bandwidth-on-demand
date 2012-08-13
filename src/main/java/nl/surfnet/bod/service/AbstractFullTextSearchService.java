package nl.surfnet.bod.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import nl.surfnet.bod.util.FullTextSearchContext;

import org.hibernate.search.jpa.FullTextQuery;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Service interface to abstract full text search functionality
 * 
 * @param <T>
 *          DomainObject
 */
public abstract class AbstractFullTextSearchService<T> {

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
   * @return List<T> result list
   */
  @SuppressWarnings("unchecked")
  public List<T> searchFor(Class<T> entityClass, String searchText, int firstResult, int maxResults, Sort sort) {
    Query jpaQuery = createSearchQuery(searchText, sort, entityClass);

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
   * @return List<T> result list
   */
  public List<T> searchForInFilteredList(Class<T> entityClass, String searchText, int firstResult, int maxResults,
      Sort sort, List<T> filteredItems) {

    List<T> resultList = searchFor(entityClass, searchText, firstResult, maxResults, sort);

    resultList = intersectFullTextResultAndFilterResult(filteredItems, resultList);

    // limit to size of resultList
    return resultList.subList(firstResult, Math.min(firstResult + maxResults, firstResult + resultList.size()));
  }

  public long countSearchFor(Class<T> entityClass, String searchText) {
    FullTextQuery jpaQuery = createSearchQuery(searchText, null, entityClass);

    return jpaQuery.getResultList().size();
  }

  @SuppressWarnings("unchecked")
  public long countSearchForInFilteredList(Class<T> entityClass, String searchText, List<T> filteredItems) {
    FullTextQuery jpaQuery = createSearchQuery(searchText, null, entityClass);

    List<T> resultList = intersectFullTextResultAndFilterResult(filteredItems, jpaQuery.getResultList());

    return resultList.size();
  }

  protected abstract EntityManager getEntityManager();

  private FullTextQuery createSearchQuery(String searchText, Sort sort, Class<T> entityClass) {
    FullTextSearchContext<T> fullTextSearchContext = new FullTextSearchContext<T>(getEntityManager(), entityClass);

    return fullTextSearchContext.getFullTextQueryForKeywordOnAllAnnotedFields(searchText, sort);
  }

  /**
   * FInds objects that both list have in common. When one or both lists are
   * empty, no elements are found
   * 
   * @param filteredItems
   *          List<T> List with result of filter
   * @param resultList
   *          List<T> List with result of search
   * @return List<T> List with common objects
   */
  private List<T> intersectFullTextResultAndFilterResult(List<T> filteredItems, List<T> resultList) {
    if (!CollectionUtils.isEmpty(filteredItems)) {
      resultList = Lists.newArrayList(Sets.intersection(Sets.newHashSet(resultList), Sets.newHashSet(filteredItems)));
    }
    else {
      resultList = filteredItems;
    }

    return resultList;
  }

}