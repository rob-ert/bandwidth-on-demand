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