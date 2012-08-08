package nl.surfnet.bod.util;

import javax.persistence.EntityManager;

import org.hibernate.search.annotations.Field;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;

@RunWith(MockitoJUnitRunner.class)
public class FullTextSearchContextTest {

  private final Class<?> entity = TestEntity.class;

  @Mock
  private EntityManager entityManager;

  private FullTextSearchContext ftsContext = new FullTextSearchContext(entityManager, entity);

  @Test
  public void testGetJpaQueryForKeywordOnAllAnnotedFields() {
    assertThat(ftsContext.getIndexedFields(entity), arrayContaining("firstName", "lastName"));
  }

  public class TestEntity {

    @Field
    private String firstName;

    @Field
    private String lastName;

    private String occupation;
  }

}
