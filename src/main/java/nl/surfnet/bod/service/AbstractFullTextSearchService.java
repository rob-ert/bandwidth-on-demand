package nl.surfnet.bod.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import nl.surfnet.bod.util.FullTextSearchContext;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.hibernate.search.jpa.FullTextQuery;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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
   * @return List<K> result list
   */
  public List<T> searchForInFilteredList(Class<K> entityClass, String searchText, int firstResult, int maxResults,
      Sort sort, RichUserDetails userDetails, List<T> filteredItems) {

    List<K> resultList = searchFor(entityClass, searchText, firstResult, maxResults, sort);

    List<T> viewList = transformToView(resultList, userDetails);
    viewList = intersectFullTextResultAndFilterResult(filteredItems, viewList);

    // limit to size of resultList
    return viewList.subList(firstResult, Math.min(firstResult + maxResults, firstResult + resultList.size()));
  }

  public long countSearchFor(Class<K> entityClass, String searchText) {
    FullTextQuery jpaQuery = createSearchQuery(searchText, null, entityClass);

    return jpaQuery.getResultList().size();
  }

  @SuppressWarnings("unchecked")
  public long countSearchForInFilteredList(Class<K> entityClass, String searchText, List<T> filteredItems) {
    FullTextQuery jpaQuery = createSearchQuery(searchText, entityClass);

    return intersectFullTextResultAndFilterResult(filteredItems, jpaQuery.getResultList()).size();
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
   * @param filteredItems
   *          List<K> List with result of filter
   * @param resultList
   *          List<K> List with result of search
   * @return List<K> List with common objects
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