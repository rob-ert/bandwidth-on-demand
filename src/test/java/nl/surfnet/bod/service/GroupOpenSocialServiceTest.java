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
package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.Collection;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.repo.LogEventRepo;
import nl.surfnet.bod.support.MockHttpServer;
import nl.surfnet.bod.util.Environment;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ByteArrayResource;

@RunWith(MockitoJUnitRunner.class)
public class GroupOpenSocialServiceTest {

  private static MockHttpServer mockServer;

  @Mock
  private LogEventRepo logEventRepo;

  @Mock
  private LogEventService logEventService;

  @Mock
  private Environment environment;

  @InjectMocks
  private GroupOpenSocialService subject;

  @Before
  public void onSetUp() throws Exception {
    mockServer = new MockHttpServer(8088);
    mockServer.startServer();

    when(environment.getOpenSocialOAuthKey()).thenReturn("key");
    when(environment.getOpenSocialOAuthSecret()).thenReturn("secret");
    when(environment.getOpenSocialUrl()).thenReturn("http://localhost:8088/social/rest");
  }

  @AfterClass
  public static void tearDown() throws Exception {
    mockServer.stopServer();
  }

  @Test
  public void getGroupsShouldReturnOneGroup() {
    final String nameId = "urn:collab:person:surfguest.nl:alanvdam";

    mockServer
        .addResponse(
            "/social/rest/groups/".concat(nameId),
            new ByteArrayResource(
                ("{\"startIndex\":0,\"totalResults\":1,\"entry\":["
                    + "{\"id\":\"urn:collab:group:surfteams.nl:nl:surfnet:diensten:bandwidth-on-demand\","
                    + "\"title\":\"bandwidth-on-demand\",\"description\":\"The BoD development team\"}]"
                    + ",\"itemsPerPage\":1}").getBytes()));

    Collection<UserGroup> groups = subject.getGroups(nameId);

    assertThat(groups, hasSize(1));

    UserGroup group = groups.iterator().next();
    assertThat(group.getName(), is("bandwidth-on-demand"));
    assertThat(group.getId(), is("urn:collab:group:surfteams.nl:nl:surfnet:diensten:bandwidth-on-demand"));

    assertThat(mockServer.getCallCounter(), is(1));
  }
}
