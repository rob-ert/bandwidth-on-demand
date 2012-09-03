package nl.surfnet.bod.search;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.hibernate.Session;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.surfnet.bod.domain.PhysicalPort;

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
    List<PhysicalPort> results = searchQuery(searchQuery).getResultList();
    if (results == null || results.size() == 0) {
      searchQuery = "*" + searchQuery + "*";
      results = searchQuery(searchQuery).getResultList();
    }
    return results;
  }

  private Query searchQuery(String searchQuery) throws ParseException {

    final String[] physicalPortField = { "nocLabel", "managerLabel", "bodPortId", "nmsPortId" };
    final FullTextEntityManager ftEm = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
    final Analyzer customAnalyzer = ftEm.getSearchFactory().getAnalyzer("customanalyzer");
    final QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_35, physicalPortField, customAnalyzer);
    parser.setAllowLeadingWildcard(true);
    final org.apache.lucene.search.Query luceneQuery = parser.parse(searchQuery);
    final FullTextQuery query = ftEm.createFullTextQuery(luceneQuery, PhysicalPort.class);

    return query;
  }
}
