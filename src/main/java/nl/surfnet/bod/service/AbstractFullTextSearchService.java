package nl.surfnet.bod.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import nl.surfnet.bod.util.FullTextSearchContext;

import org.hibernate.search.jpa.FullTextQuery;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
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
   * Peforms a full text search on the given searchText, if filteredItems are
   * present both result will be combined, and the intersection will be returned
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
   *          nullable list of already found items
   * 
   * @return List<T> result list
   */
  public List<T> searchFor(Class<T> entityClass, String searchText, int firstResult, int maxResults, Sort sort,
      List<T> filteredItems) {
    Query jpaQuery = createSearchQuery(searchText, sort, entityClass);

    jpaQuery.setFirstResult(firstResult);
    jpaQuery.setMaxResults(maxResults);
    @SuppressWarnings("unchecked")
    List<T> resultList = jpaQuery.getResultList();

    resultList = intersectFullTextResultAndFilterResult(filteredItems, resultList);

    // limit to size of resultList
    return resultList.subList(firstResult, Math.min(firstResult + maxResults, firstResult + resultList.size()));
  }

  @SuppressWarnings("unchecked")
  public long countSearchFor(Class<T> entityClass, String searchText, List<T> filteredItems) {
    FullTextQuery jpaQuery = createSearchQuery(searchText, null, entityClass);

    List<T> resultList = intersectFullTextResultAndFilterResult(filteredItems, jpaQuery.getResultList());

    return resultList.size();
  }

  protected abstract EntityManager getEntityManager();

  private FullTextQuery createSearchQuery(String searchText, Sort sort, Class<T> entityClass) {
    FullTextSearchContext<T> fullTextSearchContext = new FullTextSearchContext<T>(getEntityManager(), entityClass);

    return fullTextSearchContext.getFullTextQueryForKeywordOnAllAnnotedFields(searchText, sort);
  }

  private List<T> intersectFullTextResultAndFilterResult(List<T> filteredItems, List<T> resultList) {
    if (!CollectionUtils.isEmpty(filteredItems)) {
      resultList = Lists.newArrayList(Sets.intersection(Sets.newHashSet(resultList), Sets.newHashSet(filteredItems)));
    }
    return resultList;
  }

}