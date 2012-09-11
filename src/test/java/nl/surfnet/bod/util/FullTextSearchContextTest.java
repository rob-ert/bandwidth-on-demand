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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import javax.persistence.EntityManager;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.SortField;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;

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

  @Test
  public void testConvertLuceneSortForPrimitiveAscending() {
    String[] indexedFields = { "scale" };
    Sort springSort = new Sort(Sort.Direction.ASC, indexedFields);
    org.apache.lucene.search.Sort luceneSort = ftsContext.convertToLuceneSort(springSort, indexedFields);

    assertThat(luceneSort.getSort().length, is(1));
    assertThat(luceneSort.getSort()[0].getField(), is("scale"));
    assertThat(luceneSort.getSort()[0].getType(), is(SortField.INT));
    assertThat(luceneSort.getSort()[0].getReverse(), is(false));
  }

  @Test
  public void testConvertLuceneSortForPrimitiveDescending() {
    String[] indexedFields = { "scale" };
    Sort springSort = new Sort(Sort.Direction.DESC, indexedFields);
    org.apache.lucene.search.Sort luceneSort = ftsContext.convertToLuceneSort(springSort, indexedFields);

    assertThat(luceneSort.getSort()[0].getReverse(), is(true));
  }

  @Test
  public void testConvertLuceneSortForNonExisting() {
    String[] indexedFields = { "scale" };
    Sort springSort = new Sort(Sort.Direction.ASC, "nonExistingField");
    org.apache.lucene.search.Sort luceneSort = ftsContext.convertToLuceneSort(springSort, indexedFields);

    assertThat(luceneSort, nullValue());
  }

  @Test
  public void testConvertLuceneSortForNonIndexed() {
    String[] indexedFields = {};
    Sort springSort = new Sort(Sort.Direction.ASC, "notIndexed");
    org.apache.lucene.search.Sort luceneSort = ftsContext.convertToLuceneSort(springSort, indexedFields);

    assertThat(luceneSort, nullValue());
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
