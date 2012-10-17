/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.search;

import java.util.Collections;
import java.util.List;

import nl.surfnet.bod.service.AbstractFullTextSearchService;
import nl.surfnet.bod.util.TestEntity;
import nl.surfnet.bod.util.TestView;
import nl.surfnet.bod.web.WebUtils;
import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

public class TestSearchController extends AbstractSearchableSortableListController<TestView, TestEntity> {

  private AbstractFullTextSearchService<TestEntity> testFullTextService;
  private List<TestEntity> testEntities;

  @Override
  protected String listUrl() {
    return WebUtils.LIST;
  }

  @Override
  protected List<TestView> list(int firstPage, int maxItems, Sort sort, Model model) {
    return transformToView(testEntities, null);
  }

  @Override
  public long count(Model model) {
    return testEntities.size();
  }

  @Override
  protected AbstractFullTextSearchService<TestEntity> getFullTextSearchableService() {
    return testFullTextService;
  }

  public void setTestFullTextService(AbstractFullTextSearchService<TestEntity> service) {
    this.testFullTextService = service;
  }

  public void setTestEntities(List<TestEntity> list) {
    this.testEntities = list;
  }

  @Override
  public List<TestView> transformToView(List<TestEntity> entities, RichUserDetails user) {
    return FluentIterable.from(entities).transform(new Function<TestEntity, TestView>() {
      @Override
      public TestView apply(TestEntity input) {
        return new TestView(input);
      }
    }).toImmutableList();
  }

  @Override
  public List<Long> handleListFromController(Model model) {
    return Collections.emptyList();
  }

}
