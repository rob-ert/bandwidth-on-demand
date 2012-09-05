package nl.surfnet.bod.search;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import nl.surfnet.bod.event.LogEvent;

public class LogEventIndexAndSearchTest extends AbstractIndexAndSearch<LogEvent> {

  public LogEventIndexAndSearchTest() {
    super(LogEvent.class);
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

    List<LogEvent> logEvents = getSearchQuery("klimaat");
    // nothing indexed so nothing should be found
    assertThat(logEvents.size(), is(0));

    index();

    logEvents = getSearchQuery("gamma");
    // (N.A.)
    assertThat(logEvents.size(), is(0));

    logEvents = getSearchQuery("NOC engineers");
    // (1st & 2nd event)
    assertThat(logEvents.size(), is(2));
    
    logEvents = getSearchQuery("klimaat1");
    // (2nd event)
    assertThat(logEvents.size(), is(1));
    
    logEvents = getSearchQuery("klimaat");
    // (1st & /2nd event)
    assertThat(logEvents.size(), is(2));

  }

}