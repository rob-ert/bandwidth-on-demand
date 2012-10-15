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
