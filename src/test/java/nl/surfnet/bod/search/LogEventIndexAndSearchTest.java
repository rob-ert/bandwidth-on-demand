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
package nl.surfnet.bod.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;

import nl.surfnet.bod.event.LogEvent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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