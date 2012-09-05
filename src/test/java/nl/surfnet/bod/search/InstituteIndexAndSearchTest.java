package nl.surfnet.bod.search;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import nl.surfnet.bod.domain.Institute;

public class InstituteIndexAndSearchTest extends AbstractIndexAndSearch<Institute> {

  public InstituteIndexAndSearchTest() {
    super(Institute.class);
  }

  @Before
  public void setUp() {
    initEntityManager();
  }

  @After
  public void tearDown() {
    closeEntityManager();
  }

  @Test
  public void testIndexAndSearch() throws Exception {

    List<Institute> institutes = getSearchQuery("Mock_Klantnaam");
    // nothing indexed so nothing should be found
    assertThat(institutes.size(), is(0));

    index();

    institutes = getSearchQuery("Mock_Klantnaam");
    // (Mock_Klantnaam)
    assertThat(institutes.size(), is(1));

    institutes = getSearchQuery("de");
    // (De Kempel, Deltion College, Design Academy, Dienst Uitvoering Onderwijs)
    assertThat(institutes.size(), is(4));

  }

}