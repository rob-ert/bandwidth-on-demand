/**
 * Copyright (c) 2012, SURFnet BV
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
package nl.surfnet.bod.nsi.v2;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.EnumSet;

import javax.xml.ws.Holder;

import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.oauth.NsiScope;
import nl.surfnet.bod.repo.ConnectionV2Repo;
import nl.surfnet.bod.service.ConnectionServiceV2;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.security.Security;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ogf.schemas.nsi._2013._04.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2013._04.connection.types.PathType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationRequestCriteriaType;
import org.ogf.schemas.nsi._2013._04.connection.types.ScheduleType;
import org.ogf.schemas.nsi._2013._04.connection.types.StpType;
import org.ogf.schemas.nsi._2013._04.framework.headers.CommonHeaderType;
import org.ogf.schemas.nsi._2013._04.framework.types.TypeValuePairListType;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceProviderV2WsTest {

  @InjectMocks private ConnectionServiceProviderV2Ws subject;

  @Mock private Environment bodEnvironmentMock;
  @Mock private ConnectionV2Repo connectionRepoMock;
  @Mock private ConnectionServiceV2 connectionService;

  @Before
  public void setUp() {
    Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.allOf(NsiScope.class)).create());
  }

  @After
  public void tearDown() {
    Security.clearUserDetails();
  }

  @Test
  public void should_create_connection_on_initial_reserve() throws Exception {
    Holder<String> connectionIdHolder = new Holder<String>();
    Holder<CommonHeaderType> headerHolder = new Holder<>(headers());
    subject.reserve(connectionIdHolder, "globalReservationId", "description", initialReservationCriteria(), headerHolder);

    assertThat(connectionIdHolder.value, is(notNullValue()));

    ArgumentCaptor<ConnectionV2> connection = ArgumentCaptor.forClass(ConnectionV2.class);
    verify(connectionService).reserve(connection.capture(), org.mockito.Matchers.isA(NsiRequestDetails.class), eq(Security.getUserDetails()));
    assertThat(connection.getValue().getDesiredBandwidth(), is(100));
    assertThat(connection.getValue().getGlobalReservationId(), is("globalReservationId"));
    assertThat(connection.getValue().getReservationState(), is(nullValue()));
    assertThat(headerHolder.value.getReplyTo(), is(nullValue()));
  }

  @Test
  public void should_require_nsi_reserve_scope_on_reserve() throws Exception {
    Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.noneOf(NsiScope.class)).create());

    try {
      subject.reserve(new Holder<String>(), null, null, initialReservationCriteria(), new Holder<>(headers()));
      fail("ServiceException expected");
    } catch (ServiceException expected) {
      assertThat(expected.getFaultInfo().getText(), is("Unauthorized"));
    }
  }

  @Test
  public void should_reject_reserve_with_connection_id_until_modify_is_supported() {
    try {
      subject.reserve(new Holder<>("connectionId"), null, null, initialReservationCriteria(), new Holder<>(headers()));
      fail("ServiceException expected");
    } catch (ServiceException expected) {
      assertThat(expected.getFaultInfo().getText(), is("Not Supported"));
    }
  }

  private CommonHeaderType headers() {
    return new CommonHeaderType().withCorrelationId("correlationId").withProviderNSA("providerNSA").withRequesterNSA("requesterNSA").withReplyTo("replyTo");
  }

  private ReservationRequestCriteriaType initialReservationCriteria() {
    return new ReservationRequestCriteriaType()
        .withBandwidth(100)
        .withPath(
            new PathType().withSourceSTP(new StpType().withNetworkId("networkId").withLocalId("source")).withDestSTP(
                new StpType().withNetworkId("networkId").withLocalId("dest"))).withSchedule(new ScheduleType())
        .withServiceAttributes(new TypeValuePairListType());
  }
}
