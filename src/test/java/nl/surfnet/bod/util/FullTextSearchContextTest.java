/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
