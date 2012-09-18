package nl.surfnet.bod.search;

import java.util.List;

import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.util.TestEntity;
import nl.surfnet.bod.util.TestView;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;

import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;

public class TestSearchController extends AbstractSearchableSortableListController<TestView, TestEntity> {

  private AbstractFullTextSearchService<TestView, TestEntity> testFullTextService;
  private List<TestEntity> testEntities;

  @Override
  protected String listUrl() {
    return WebUtils.LIST;
  }

  @Override
  protected List<TestView> list(int firstPage, int maxItems, Sort sort, Model model) {
    return testFullTextService.transformToView(testEntities, null);
  }

  @Override
  public long count() {
    return testEntities.size();
  }

  @Override
  protected AbstractFullTextSearchService<TestView, TestEntity> getFullTextSearchableService() {
    return testFullTextService;
  }

  public void setTestFullTextService(AbstractFullTextSearchService<TestView, TestEntity> service) {
    this.testFullTextService = service;
  }

  public void setTestEntities(List<TestEntity> list) {
    this.testEntities = list;
  }

}
