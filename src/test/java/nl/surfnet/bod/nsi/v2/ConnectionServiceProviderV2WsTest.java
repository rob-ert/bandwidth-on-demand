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

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.xml.ws.Holder;

import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.oauth.NsiScope;
import nl.surfnet.bod.repo.ConnectionV2Repo;
import nl.surfnet.bod.service.ConnectionServiceV2;
import nl.surfnet.bod.support.ConnectionV2Factory;
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
import org.ogf.schemas.nsi._2013._04.connection.provider.QuerySummarySyncFailed;
import org.ogf.schemas.nsi._2013._04.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2013._04.connection.types.PathType;
import org.ogf.schemas.nsi._2013._04.connection.types.ProvisionStateEnumType;
import org.ogf.schemas.nsi._2013._04.connection.types.QuerySummaryResultType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationRequestCriteriaType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationStateEnumType;
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

  private Holder<CommonHeaderType> headerHolder;

  @Before
  public void setUp() {
    Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.allOf(NsiScope.class)).create());
    headerHolder = new Holder<>(headers());
  }

  @After
  public void tearDown() {
    Security.clearUserDetails();
  }

  @Test
  public void should_create_connection_on_initial_reserve() throws Exception {
    Holder<String> connectionIdHolder = new Holder<String>();
    subject.reserve(connectionIdHolder, "globalReservationId", "description", initialReservationCriteria(), headerHolder);

    assertThat(connectionIdHolder.value, is(notNullValue()));

    ArgumentCaptor<ConnectionV2> connection = ArgumentCaptor.forClass(ConnectionV2.class);
    ArgumentCaptor<NsiRequestDetails> nsiRequestDetails = ArgumentCaptor.forClass(NsiRequestDetails.class);
    verify(connectionService).reserve(connection.capture(), nsiRequestDetails.capture(), eq(Security.getUserDetails()));
    assertThat(connection.getValue().getDesiredBandwidth(), is(100));
    assertThat(connection.getValue().getGlobalReservationId(), is("globalReservationId"));
    assertThat(connection.getValue().getReservationState(), is(nullValue()));
    assertThat(nsiRequestDetails.getValue().getReplyTo(), is("replyTo"));
    assertThat(headerHolder.value.getReplyTo(), is(nullValue()));
  }

  @Test
  public void should_require_nsi_reserve_scope_on_reserve() throws Exception {
    Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.noneOf(NsiScope.class)).create());

    try {
      subject.reserve(new Holder<String>(), null, null, initialReservationCriteria(), headerHolder);
      fail("ServiceException expected");
    } catch (ServiceException expected) {
      assertThat(expected.getFaultInfo().getText(), is("Unauthorized"));
    }
  }

  @Test
  public void should_reject_reserve_with_connection_id_until_modify_is_supported() {
    try {
      subject.reserve(new Holder<>("connectionId"), null, null, initialReservationCriteria(), headerHolder);
      fail("ServiceException expected");
    } catch (ServiceException expected) {
      assertThat(expected.getFaultInfo().getText(), is("Not Supported"));
    }
  }

  @Test
  public void should_only_commit_when_reserve_held() {
    try {
      when(connectionRepoMock.findByConnectionId("connectionId")).thenReturn(new ConnectionV2Factory().setReservationState(ReservationStateEnumType.RESERVE_START).create());

      subject.reserveCommit("connectionId", headerHolder);

      fail("ServiceException expected");
    } catch (ServiceException expected) {
      assertThat(expected.getFaultInfo().getText(), is("Not Applicable"));
    }
  }

  @Test
  public void should_commit_when_reserve_held() throws Exception {
      when(connectionRepoMock.findByConnectionId("connectionId")).thenReturn(new ConnectionV2Factory().setReservationState(ReservationStateEnumType.RESERVE_HELD).create());

      subject.reserveCommit("connectionId", headerHolder);

      assertThat(headerHolder.value.getReplyTo(), is(nullValue()));
      verify(connectionService).asyncReserveCommit(eq("connectionId"), org.mockito.Matchers.isA(NsiRequestDetails.class));
  }

  @Test
  public void should_require_nsi_reserve_scope_on_reserve_commit() throws Exception {
    Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.noneOf(NsiScope.class)).create());

    try {
      subject.reserveCommit("connectionId", headerHolder);
      fail("ServiceException expected");
    } catch (ServiceException expected) {
      assertThat(expected.getFaultInfo().getText(), is("Unauthorized"));
    }
  }

  @Test
  public void should_only_abort_when_reserve_held() {
    try {
      when(connectionRepoMock.findByConnectionId("connectionId")).thenReturn(new ConnectionV2Factory().setReservationState(ReservationStateEnumType.RESERVE_START).create());

      subject.reserveAbort("connectionId", headerHolder);

      fail("ServiceException expected");
    } catch (ServiceException expected) {
      assertThat(expected.getFaultInfo().getText(), is("Not Applicable"));
    }
  }

  @Test
  public void should_abort_when_reserve_held() throws Exception {
    when(connectionRepoMock.findByConnectionId("connectionId")).thenReturn(new ConnectionV2Factory().setReservationState(ReservationStateEnumType.RESERVE_HELD).create());

    subject.reserveAbort("connectionId", headerHolder);

    assertThat(headerHolder.value.getReplyTo(), is(nullValue()));
    verify(connectionService).asyncReserveAbort(eq("connectionId"), org.mockito.Matchers.isA(NsiRequestDetails.class), eq(Security.getUserDetails()));
  }

  @Test
  public void should_require_nsi_reserve_scope_on_reserve_abort() throws Exception {
    Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.noneOf(NsiScope.class)).create());

    try {
      subject.reserveAbort("connectionId", headerHolder);
      fail("ServiceException expected");
    } catch (ServiceException expected) {
      assertThat(expected.getFaultInfo().getText(), is("Unauthorized"));
    }
  }

  @Test
  public void should_only_provision_when_released() {
    try {
      when(connectionRepoMock.findByConnectionId("connectionId")).thenReturn(new ConnectionV2Factory().setProvisionState(ProvisionStateEnumType.PROVISIONED).create());

      subject.provision("connectionId", headerHolder);

      fail("ServiceException expected");
    } catch (ServiceException expected) {
      assertThat(expected.getFaultInfo().getText(), is("Not Applicable"));
    }
  }

  @Test
  public void should_provision_when_released() throws Exception {
      when(connectionRepoMock.findByConnectionId("connectionId")).thenReturn(new ConnectionV2Factory().setProvisionState(ProvisionStateEnumType.RELEASED).create());

      subject.provision("connectionId", headerHolder);

      assertThat(headerHolder.value.getReplyTo(), is(nullValue()));
      verify(connectionService).asyncProvision(eq("connectionId"), org.mockito.Matchers.isA(NsiRequestDetails.class));
  }

  @Test
  public void should_require_nsi_provision_scope_on_provision() throws Exception {
    Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.noneOf(NsiScope.class)).create());

    try {
      subject.provision("connectionId", headerHolder);
      fail("ServiceException expected");
    } catch (ServiceException expected) {
      assertThat(expected.getFaultInfo().getText(), is("Unauthorized"));
    }
  }

  @Test
  public void should_terminate() throws Exception {
      when(connectionRepoMock.findByConnectionId("connectionId")).thenReturn(new ConnectionV2Factory().create());

      subject.terminate("connectionId", headerHolder);

      assertThat(headerHolder.value.getReplyTo(), is(nullValue()));
      verify(connectionService).asyncTerminate(eq("connectionId"), org.mockito.Matchers.isA(NsiRequestDetails.class), eq(Security.getUserDetails()));
  }

  @Test
  public void should_require_nsi_terminate_scope_on_terminate() throws Exception {
    Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.noneOf(NsiScope.class)).create());

    try {
      subject.terminate("connectionId", headerHolder);
      fail("ServiceException expected");
    } catch (ServiceException expected) {
      assertThat(expected.getFaultInfo().getText(), is("Unauthorized"));
    }
  }

  @Test
  public void should_require_nsi_query_scope_on_query_summary() throws Exception {
    Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.noneOf(NsiScope.class)).create());

    try {
      subject.querySummary(Collections.<String>emptyList(), null, headerHolder);
      fail("ServiceException expected");
    } catch (ServiceException expected) {
      assertThat(expected.getFaultInfo().getText(), is("Unauthorized"));
    }
  }

  @Test
  public void should_return_query_information_synchronously() throws Exception {
    ConnectionV2 connection = new ConnectionV2Factory().setRequesterNsa("requesterNSA").create();
    when(connectionService.querySummarySync(Collections.<String>emptyList(), Collections.<String>emptyList(), "requesterNSA")).thenReturn(Collections.singletonList(connection));

    List<QuerySummaryResultType> results = subject.querySummarySync(Collections.<String>emptyList(), Collections.<String>emptyList(), headerHolder);

    assertThat(results, hasSize(1));
    QuerySummaryResultType result = results.get(0);
    assertThat(result.getConnectionId(), is(connection.getConnectionId()));
    assertThat(result.getRequesterNSA(), is("requesterNSA"));
  }

  @Test
  public void should_require_nsi_query_scope_on_query_summary_sync() throws Exception {
    Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.noneOf(NsiScope.class)).create());

    try {
      subject.querySummarySync(Collections.<String>emptyList(), null, headerHolder);
      fail("QuerySummarySyncFailed expected");
    } catch (QuerySummarySyncFailed expected) {
      assertThat(expected.getFaultInfo().getServiceException().getText(), is("Unauthorized"));
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
