/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
  protected List<? extends TestView> list(int firstPage, int maxItems, Sort sort, Model model) {
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
  protected List<? extends TestView> transformToView(List<? extends TestEntity> entities, RichUserDetails user) {
    return FluentIterable.from(entities).transform(new Function<TestEntity, TestView>() {
      @Override
      public TestView apply(TestEntity input) {
        return new TestView(input);
      }
    }).toList();
  }

  @Override
  protected List<Long> getIdsOfAllAllowedEntries(Model model, Sort sort) {
    return Collections.emptyList();
  }

}
