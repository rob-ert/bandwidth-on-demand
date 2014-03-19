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
package nl.surfnet.bod.web;

import static org.junit.Assert.assertThat;

import org.junit.Before;

import com.google.common.net.HttpHeaders;
import javax.servlet.http.HttpServletResponse;
import nl.surfnet.bod.service.TopologyService;
import nl.surfnet.bod.util.XmlUtils;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ogf.schemas.nml._2013._05.base.TopologyType;
import org.ogf.schemas.nsi._2013._09.topology.NSAType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


@RunWith(MockitoJUnitRunner.class)
public class NsiTopologyControllerTest {

  @Mock private TopologyService topologyService;

  @InjectMocks private NsiTopologyController subject = new NsiTopologyController();

  private static final DateTime TIMESTAMP = new DateTime(2013, 11, 4, 15, 10, 7, DateTimeZone.UTC);

  private MockHttpServletRequest request = new MockHttpServletRequest();

  private MockHttpServletResponse response = new MockHttpServletResponse();

  private TopologyType topology = new TopologyType().withVersion(XmlUtils.toGregorianCalendar(TIMESTAMP));

  @Before
  public void setUp() {
    Mockito.when(topologyService.nsiTopology()).thenReturn(topology);
  }

  @Test
  public void should_send_new_content_if_last_modified_is_not_set() throws Exception {
    subject.topology(request, response);

    assertThat(response.getStatus(), Matchers.is(HttpServletResponse.SC_OK));
    assertThat(response.getHeaderValue(HttpHeaders.LAST_MODIFIED), Matchers.<Object>is("Mon, 04 Nov 2013 15:10:07 GMT"));
  }

  @Test
  public void should_send_new_content_if_last_modified_does_not_match() throws Exception {
    request.addHeader(HttpHeaders.IF_MODIFIED_SINCE, "Mon, 04 Nov 2013 15:00:07 GMT");

    subject.topology(request, response);

    assertThat(response.getStatus(), Matchers.is(HttpServletResponse.SC_OK));
    assertThat(response.getHeaderValue(HttpHeaders.LAST_MODIFIED), Matchers.<Object>is("Mon, 04 Nov 2013 15:10:07 GMT"));
  }

  @Test
  public void should_respond_with_not_modified_if_last_modified_since_matches() throws Exception {
    request.addHeader(HttpHeaders.IF_MODIFIED_SINCE, "Mon, 04 Nov 2013 15:10:07 GMT");

    subject.topology(request, response);

    assertThat(response.getStatus(), Matchers.is(HttpServletResponse.SC_NOT_MODIFIED));
  }
}
