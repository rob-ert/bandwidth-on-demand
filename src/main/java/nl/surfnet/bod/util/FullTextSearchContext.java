package nl.surfnet.bod.util;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

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

  public Query getJpaQueryForKeywordOnAllAnnotedFields(String keyword) {
    QueryBuilder fullTextQueryBuilder = getFullTextEntityManager(entityManager).getSearchFactory().buildQueryBuilder()
        .forEntity(entity).get();

    org.apache.lucene.search.Query luceneQuery = fullTextQueryBuilder.keyword().onFields(getIndexedFields(entity))
        .matching(keyword).createQuery();

    return getJpaQuery(luceneQuery);
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

  private Query getJpaQuery(org.apache.lucene.search.Query luceneQuery) {
    return getFullTextEntityManager(entityManager).createFullTextQuery(luceneQuery, entity);
  }

}