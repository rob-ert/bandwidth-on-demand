package nl.surfnet.bod.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.data.domain.Sort.Order;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

/**
 * Class which holds state related to creating and executing a full text search
 * 
 */
public class FullTextSearchContext {

  private final Class<?> entity;
  private final EntityManager entityManager;

  public FullTextSearchContext(EntityManager em, Class<?> entityType) {
    this.entity = entityType;
    this.entityManager = em;
  }

  public FullTextQuery getFullTextQueryForKeywordOnAllAnnotedFields(String keyword,
      org.springframework.data.domain.Sort springSort) {
    QueryBuilder fullTextQueryBuilder = getFullTextEntityManager(entityManager).getSearchFactory().buildQueryBuilder()
        .forEntity(entity).get();

    String[] indexedFields = getIndexedFields(entity);
    org.apache.lucene.search.Query luceneQuery = fullTextQueryBuilder.keyword().onFields(indexedFields)
        .matching(keyword).createQuery();

    return getFullTextQuery(luceneQuery, convertToLuceneSort(springSort, indexedFields));
  }

  /**
   * 
   * @return String array containing all fieldNames of the given entity which
   *         are annotated with @link {@Field} to mark them indexable.
   */
  @VisibleForTesting
  String[] getIndexedFields(Class<?> entity) {
    List<String> fieldNames = Lists.newArrayList();

    java.lang.reflect.Field[] declaredFields = entity.getDeclaredFields();
    for (java.lang.reflect.Field field : declaredFields) {
      if (field.getAnnotation(Field.class) != null) {
        fieldNames.add(field.getName());
      }
    }

    return fieldNames.toArray(new String[fieldNames.size()]);
  }

  /**
   * Just to enable mocking
   */
  @VisibleForTesting
  FullTextEntityManager getFullTextEntityManager(EntityManager em) {
    return Search.getFullTextEntityManager(em);
  }

  @VisibleForTesting
  Sort convertToLuceneSort(org.springframework.data.domain.Sort springSort, String[] indexedFields) {
    List<SortField> sortFields = Lists.newArrayList();

    if (springSort != null) {
      Iterator<Order> orderIt = springSort.iterator();
      while (orderIt.hasNext()) {
        Order order = orderIt.next();
        if (order != null) {
          String propertyName = order.getProperty();
          // Is it an indexed field?
          if (Arrays.asList(indexedFields).contains(propertyName)) {
            int luceneType = LuceneSortFieldType.getLuceneTypeFor(ReflectionUtils.findField(entity, propertyName)
                .getType());

            SortField sortField = new SortField(propertyName, luceneType, !(order.isAscending()));
            sortFields.add(sortField);
          }
        }
      }
    }

    if (CollectionUtils.isEmpty(sortFields)) {
      return null;
    }
    else {
      return new Sort(sortFields.toArray(new SortField[(sortFields.size())]));
    }

  }

  private FullTextQuery getFullTextQuery(org.apache.lucene.search.Query luceneQuery, Sort sort) {
    FullTextQuery fullTextQuery = getFullTextEntityManager(entityManager).createFullTextQuery(luceneQuery, entity);

    if (sort != null) {
      fullTextQuery.setSort(sort);
    }

    return fullTextQuery;
  }

}