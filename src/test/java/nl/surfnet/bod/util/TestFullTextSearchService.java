package nl.surfnet.bod.util;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.web.security.RichUserDetails;

public class TestFullTextSearchService extends AbstractFullTextSearchService<TestView, TestEntity> {

  private EntityManager entityManager;

  @Override
  public List<TestView> transformToView(List<TestEntity> listToTransform, RichUserDetails user) {
    List<TestView> views = new ArrayList<>();
    for (TestEntity testEntity : listToTransform) {
      views.add(new TestView(testEntity));
    }

    return views;
  }

  @Override
  protected EntityManager getEntityManager() {
    return entityManager;
  }

  public void setEntityManager(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

}
