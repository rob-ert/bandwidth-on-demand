/**
 * Copyright (c) 2012, 2013 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.List;

import nl.surfnet.bod.event.LogEvent;

import org.apache.lucene.queryParser.ParseException;
import org.junit.Test;

import com.google.common.collect.Iterables;

public class LogEventIndexAndSearchTest extends AbstractIndexAndSearch<LogEvent> {

  public LogEventIndexAndSearchTest() {
    super(LogEvent.class);
  }

  @Test
  public void findByNonExistingStringShouldGiveNoLogEvents() throws ParseException {
    List<LogEvent> logEvents = searchFor("gamma");

    assertThat(logEvents, hasSize(0));
  }

  @Test
  public void findLogEventsWithSpace() throws ParseException {
    List<LogEvent> logEvents = searchFor("NOC engineers");

    assertThat(logEvents, hasSize(2));
  }

  @Test
  public void findLogEventsByPartialString() throws Exception {
    List<LogEvent> logEvents = searchFor("klimaat1");
    assertThat(logEvents, hasSize(1));

    logEvents = searchFor("klimaat");
    assertThat(logEvents, hasSize(2));
  }

  @Test
  public void findLogEventByAdminGroup() throws ParseException {
    final String adminGroup = "urn:surfguest:ict-managers";
    List<LogEvent> logEvents = searchFor("\"".concat(adminGroup).concat("\""));

    assertThat(logEvents, hasSize(1));
    assertThat(Iterables.getOnlyElement(logEvents).getAdminGroups(), hasItem(adminGroup));
  }

  @Test
  public void findLogEventsByMultipleAdminGroups() throws ParseException {
    List<LogEvent> firstLogEvents = searchFor("\"urn:surfguest:oneusers\"");
    List<LogEvent> secondLogEvents = searchFor("\"urn:surfguest:twousers\"");

    assertThat(Iterables.getOnlyElement(firstLogEvents), is(Iterables.getOnlyElement(secondLogEvents)));
  }

}