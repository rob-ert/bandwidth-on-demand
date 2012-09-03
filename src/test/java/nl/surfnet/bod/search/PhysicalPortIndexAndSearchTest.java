package nl.surfnet.bod.search;

import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.util.FullTextSearchContext;

import org.apache.lucene.queryParser.ParseException;
import org.hibernate.Session;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.search.FullTextSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Example testcase for Hibernate Search
 */

public class PhysicalPortIndexAndSearchTest {

  private EntityManagerFactory emf;

  private EntityManager em;

  private static Logger log = LoggerFactory.getLogger(PhysicalPortIndexAndSearchTest.class);

  @Before
  public void setUp() {
    initHibernate();
  }

  @After
  public void tearDown() {
    purge();
  }

  @Test
  public void testIndexAndSearch() throws Exception {

    List<PhysicalPort> physicalPorts = search("ut");
    // nothing indexed so nothing should be found
    assertThat(physicalPorts.size(), is(0));

    index();

    physicalPorts = search("gamma");
    // (N.A.)
    assertThat(physicalPorts.size(), is(0));

    physicalPorts = search("ut");
    // (UT One, UT Two)
    assertThat(physicalPorts.size(), is(2));

    physicalPorts = search("Ut");
    // (UT One, UT Two)
    assertThat(physicalPorts.size(), is(2));

    physicalPorts = search("Mock");
    // (Mock_Ut002A_OME01_ETH-1-2-4, Mock_Ut001A_OME01_ETH-1-2-1)
    assertThat(physicalPorts.size(), is(2));

    physicalPorts = search("ETH-1-2-4");
    // (Mock_Ut002A_OME01_ETH-1-2-4)
    assertThat(physicalPorts.size(), is(1));
    assertThat(physicalPorts.get(0).getNocLabel(), equalTo("Mock_Ut002A_OME01_ETH-1-2-4"));

    physicalPorts = search("OME");
    // (Mock_Ut002A_OME01_ETH-1-2-4, Mock_Ut001A_OME01_ETH-1-2-1)
    assertThat(physicalPorts.size(), is(2));
    assertThat(physicalPorts.get(0).getNocLabel(), equalTo("Mock_Ut002A_OME01_ETH-1-2-4"));
    assertThat(physicalPorts.get(1).getNocLabel(), equalTo("Mock_Ut001A_OME01_ETH-1-2-1"));
    
    physicalPorts = search("ETH-1-");
    // (Mock_Ut002A_OME01_ETH-1-2-4, Mock_Ut001A_OME01_ETH-1-2-1)
    assertThat(physicalPorts.size(), is(2));
    assertThat(physicalPorts.get(0).getNocLabel(), equalTo("Mock_Ut002A_OME01_ETH-1-2-4"));
    assertThat(physicalPorts.get(1).getNocLabel(), equalTo("Mock_Ut001A_OME01_ETH-1-2-1"));
  }

  private void initHibernate() {
    final Ejb3Configuration config = new Ejb3Configuration();
    config.configure("hibernate-search-example", new HashMap());
    emf = config.buildEntityManagerFactory();
    em = emf.createEntityManager();
  }

  private void index() {
    final FullTextSession ftSession = org.hibernate.search.Search.getFullTextSession((Session) em.getDelegate());
    try {
      ftSession.createIndexer().startAndWait();
    }
    catch (InterruptedException e) {
      log.error("Error", e);
    }
  }

  private void purge() {
    final FullTextSession ftSession = org.hibernate.search.Search.getFullTextSession((Session) em.getDelegate());
    ftSession.purgeAll(PhysicalPort.class);
    ftSession.flushToIndexes();
    ftSession.close();
    emf.close();
  }

  private List<PhysicalPort> search(String searchQuery) throws ParseException {
    List<PhysicalPort> results = getSearchQuery(searchQuery).getResultList();
    if (results == null || results.size() == 0) {
      searchQuery = "*" + searchQuery + "*";
      results = getSearchQuery(searchQuery).getResultList();
    }
    return results;
  }

  private Query getSearchQuery(String keyword) {
    final FullTextSearchContext<PhysicalPort> ftsc = new FullTextSearchContext<>(em, PhysicalPort.class);
    return ftsc.getFullTextQueryForKeywordOnAllAnnotedFields(keyword, new org.springframework.data.domain.Sort("id"));

  }
}
