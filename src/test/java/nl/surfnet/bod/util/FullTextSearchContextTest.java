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

import javax.persistence.EntityManager;

import org.apache.lucene.analysis.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;

@RunWith(MockitoJUnitRunner.class)
public class FullTextSearchContextTest {

  private final Class<TestEntity> entity = TestEntity.class;

  @Mock
  private EntityManager entityManager;

  private FullTextSearchContext<TestEntity> ftsContext;

  @Before
  public void setUp() {

    ftsContext = new FullTextSearchContext<TestEntity>(entityManager, entity) {
      @Override
      Analyzer getAnalyzer(String name) {
        return null;
      }
    };
  }

  @Test
  public void testAnnotedFields() {
    assertThat(ftsContext.findAllIndexedFields(entity), arrayContainingInAnyOrder("firstName", "lastName", "scale"));
  }

  @Test
  public void testWithEmbededFields() {
    assertThat(ftsContext.findAllIndexedFields(new TestEntityWithEmbededEntity().getClass()),
        arrayContainingInAnyOrder("shoeSize", "embed.firstName", "embed.lastName", "embed.scale"));
  }

  @Test
  public void testWithNestedEmbededFields() {
    String[] indexedFields = ftsContext.findAllIndexedFields(new TestEntityWithNestedEmbededEntity().getClass());

    assertThat(
        indexedFields,
        arrayContainingInAnyOrder("bloodPressure", "nestedEmbeded.shoeSize", "nestedEmbeded.embed.firstName",
            "nestedEmbeded.embed.lastName", "nestedEmbeded.embed.scale"));
  }

  class TestEntityWithNestedEmbededEntity {
    @IndexedEmbedded
    private TestEntityWithEmbededEntity nestedEmbeded;

    @Field
    private Double bloodPressure;

    @SuppressWarnings("unused")
    private Long notIndex;
  }

  class TestEntityWithEmbededEntity {

    @IndexedEmbedded
    private TestEntity embed;

    @SuppressWarnings("unused")
    private String noIndex;

    @Field
    private int shoeSize;
  }

  class TestEntity {
    @Field
    private String firstName;

    @Field
    private String lastName;

    @Field
    private int scale;

    @SuppressWarnings("unused")
    private TestEntity notIndexed;
  }

}
