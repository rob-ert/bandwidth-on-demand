package nl.surfnet.bod.util;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 * Class which holds state related to creating and executing a full text search
 * 
 */
public class FullTextSearchContext {

  private Class<?> entity;
  private FullTextEntityManager fullTextEntityManager;
  private QueryBuilder fullTextQueryBuilder;

  public FullTextSearchContext(EntityManager em, Class<?> entityType) {
    this.entity = entityType;
    this.fullTextEntityManager = Search.getFullTextEntityManager(em);
    this.fullTextQueryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(entityType)
        .get();
  }

  public QueryBuilder getFullTextQueryBuilder() {
    return fullTextQueryBuilder;
  }

  public Query getJpaQuery(org.apache.lucene.search.Query luceneQuery) {
    return fullTextEntityManager.createFullTextQuery(luceneQuery, entity);
  }
}
