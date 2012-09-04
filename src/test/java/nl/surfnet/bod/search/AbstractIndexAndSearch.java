package nl.surfnet.bod.search;

import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.ejb.Ejb3Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;

import nl.surfnet.bod.util.BoDInitializer;
import nl.surfnet.bod.util.FullTextSearchContext;

@SuppressWarnings({ "rawtypes", "deprecation", "unchecked" })
public class AbstractIndexAndSearch<T> {

  protected Logger log = LoggerFactory.getLogger(getClass());

  private EntityManager entityManager;

  private final Class entity;

  public AbstractIndexAndSearch(final Class clazz) {
    this.entity = clazz;
  }

  protected void initEntityManage() {
    final Ejb3Configuration configuration = new Ejb3Configuration();
    configuration.configure("hibernate-search-pu", new HashMap());
    entityManager = configuration.buildEntityManagerFactory().createEntityManager();
  }

  protected void index() {
    final BoDInitializer boDInitializer = new BoDInitializer();
    boDInitializer.setEntityManager(entityManager);
    boDInitializer.indexDatabaseContent();
  }

  protected List<T> getSearchQuery(String keyword) {
    final FullTextSearchContext<T> ftsc = new FullTextSearchContext<>(entityManager, entity);
    return ftsc.getFullTextQueryForKeywordOnAllAnnotedFields(keyword, new Sort("id")).getResultList();

  }

}
