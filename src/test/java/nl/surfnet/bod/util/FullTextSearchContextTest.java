package nl.surfnet.bod.util;

import javax.persistence.EntityManager;

import org.apache.lucene.search.SortField;
import org.hibernate.search.annotations.Field;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@RunWith(MockitoJUnitRunner.class)
public class FullTextSearchContextTest {

  private final Class<?> entity = TestEntity.class;

  @Mock
  private EntityManager entityManager;

  private FullTextSearchContext ftsContext = new FullTextSearchContext(entityManager, entity);

  @Test
  public void testGetJpaQueryForKeywordOnAllAnnotedFields() {
    assertThat(ftsContext.getIndexedFields(entity), arrayContaining("firstName", "lastName", "scale"));
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

    assertThat(luceneSort.getSort()[0].getType(), is(SortField.STRING_VAL));
  }

  public class TestEntity {

    @Field
    private String firstName;

    @Field
    private String lastName;

    @Field
    private int scale;

    private TestEntity notIndexed;
  }

}
