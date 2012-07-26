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
package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collection;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ByteArrayResource;

import nl.surfnet.bod.domain.UserGroup;
import nl.surfnet.bod.repo.LogEventRepo;
import nl.surfnet.bod.support.MockHttpServer;
import nl.surfnet.bod.util.Environment;

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
    when(environment.getOpenSocialUrl()).thenReturn("http://localhost:8088");
  }

  @AfterClass
  public static void tearDown() throws Exception {
    mockServer.stopServer();
  }

  @Test
  public void getGroupsShouldReturnOneGroup() {
    mockServer
        .addResponse(
            "/rest/groups/@me",
            new ByteArrayResource(
                ("{\"startIndex\":0,\"totalResults\":1,\"entry\":["
                    + "{\"id\":{\"groupId\":\"urn:collab:group:surfteams.nl:nl:surfnet:diensten:bandwidth-on-demand\",\"type\":\"groupId\"},"
                    + "\"title\":\"bandwidth-on-demand\",\"description\":\"The BoD development team\"}]"
                    + ",\"itemsPerPage\":1}").getBytes()));

    Collection<UserGroup> groups = subject.getGroups("urn:collab:person:surfguest.nl:alanvdam");

    assertThat(groups, hasSize(1));

    UserGroup group = groups.iterator().next();
    assertThat(group.getName(), is("bandwidth-on-demand"));
    assertThat(group.getId(), is("urn:collab:group:surfteams.nl:nl:surfnet:diensten:bandwidth-on-demand"));

    assertThat(mockServer.getCallCounter(), is(1));
  }
}
