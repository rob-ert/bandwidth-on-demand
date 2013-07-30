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
package nl.surfnet.bod.nsi.v1sc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType.PROVISIONED;
import static org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType.TERMINATED;

import java.util.EnumSet;

import nl.surfnet.bod.domain.ConnectionV1;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.nsi.v1sc.ConnectionServiceV1.ValidationException;
import nl.surfnet.bod.repo.ConnectionV1Repo;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.VirtualPortService;
import nl.surfnet.bod.support.ConnectionV1Factory;
import nl.surfnet.bod.support.NsiRequestDetailsFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.util.Environment;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryOperationType;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceV1Test {

  @Mock private ConnectionV1Repo connectionRepoMock;
  @Mock private ReservationService reservationServiceMock;
  @Mock private VirtualPortService virtualPortServiceMock;
  @Mock private Environment environmentMock;

  @InjectMocks private ConnectionServiceV1 subject;

  @Rule public ExpectedException thrown = ExpectedException.none();

  private final String PROVIDER_NSA = "urn:ogf:network:surfnet";

  private RichUserDetails userDetails;

  @Before
  public void setup() {
    userDetails = new RichUserDetailsFactory().setUsername("me").addUserGroup("admin").create();
    Security.setUserDetails(userDetails);

    when(environmentMock.getNsiProviderNsa()).thenReturn(PROVIDER_NSA);
  }

  @Test
  public void shouldUseRichUserDetailsAsCreater() throws ValidationException {
    NsiRequestDetails nsiRequestDetails = new NsiRequestDetailsFactory().create();

    ConnectionV1 connection = new ConnectionV1Factory().setProviderNsa(PROVIDER_NSA).create();

    when(virtualPortServiceMock.findByNsiV1StpId(anyString())).thenReturn(new VirtualPortFactory().setVirtualGroupAdminGroup("admin").create());
    when(connectionRepoMock.saveAndFlush(any(ConnectionV1.class))).thenReturn(connection);

    subject.reserve(connection, nsiRequestDetails, true, userDetails);

    assertThat(connection.getReservation().getUserCreated(), is("me"));
  }

  @Test
  public void queryResponseShouldContainRequesterAndProviderNsa() {
    QueryConfirmedType queryConfirmedType = subject.queryAllForRequesterNsa(QueryOperationType.SUMMARY, "urn:requester-nsa", "urn:provider-nsa");

    assertThat(queryConfirmedType.getRequesterNSA(), is("urn:requester-nsa"));
    assertThat(queryConfirmedType.getProviderNSA(), is("urn:provider-nsa"));
  }

  @Test
  public void allStatesShouldBeMapped() {
    for (ReservationStatus status : EnumSet.allOf(ReservationStatus.class)) {
      assertThat(ConnectionServiceV1.STATE_MAPPING.get(status), notNullValue());
    }
  }

  @Test
  public void aConnectionWithoutAReservationShouldBeTerminated() {
    ConnectionV1 connectionValid = new ConnectionV1Factory().setReservation(null).setCurrentState(TERMINATED).create();
    assertThat(subject.hasValidState(connectionValid), is(true));

    ConnectionV1 connectionInvalid = new ConnectionV1Factory().setReservation(null).setCurrentState(PROVISIONED).create();
    assertThat(subject.hasValidState(connectionInvalid), is(false));
  }

  @Test
  public void shouldThrowAValidationExceptionWenNotAuthorizedForPort() throws ValidationException {
    ConnectionV1 connection = new ConnectionV1Factory().setProviderNsa(PROVIDER_NSA).create();
    NsiRequestDetails requestDetails = new NsiRequestDetailsFactory().create();

    when(virtualPortServiceMock.findByNsiV1StpId(anyString())).thenReturn(new VirtualPortFactory().setVirtualGroupAdminGroup("wrong-admin").create());
    thrown.expect(ValidationException.class);
    thrown.expectMessage(containsString("Unauthorized"));

    subject.reserve(connection, requestDetails, false, userDetails);
  }

  @Test
  public void shouldThrowAValidationExceptionWhenNonUniqueConnectionIdIsUsed() throws ValidationException {
    ConnectionV1 connection = new ConnectionV1Factory().setProviderNsa(PROVIDER_NSA).create();
    NsiRequestDetails requestDetails = new NsiRequestDetailsFactory().create();

    when(connectionRepoMock.findByConnectionId(anyString())).thenReturn(new ConnectionV1());

    try {
      subject.reserve(connection, requestDetails, false, userDetails);
      fail("Excpected a ValicationException");
    } catch (ValidationException e) {
      assertThat(e.getMessage(), containsString("already exists"));
    }

    verify(connectionRepoMock, never()).save(any(ConnectionV1.class));
  }

  @Test
  public void shouldThrowAValidationExceptionWhenPortDoesNotExist() throws ValidationException {
    ConnectionV1 connection = new ConnectionV1Factory().setProviderNsa(PROVIDER_NSA).create();
    NsiRequestDetails requestDetails = new NsiRequestDetailsFactory().create();

    when(virtualPortServiceMock.findByNsiV1StpId(anyString())).thenReturn(null);
    thrown.expect(ValidationException.class);
    thrown.expectMessage(containsString("Unknown STP"));

    subject.reserve(connection, requestDetails, false, userDetails);
  }

  @Test
  public void shouldThrowAValidationExceptionWhenTheProviderNsaDoesNotMatch() throws ValidationException {
    ConnectionV1 connection = new ConnectionV1Factory().setProviderNsa("urn:ogf:network:unknown").create();
    NsiRequestDetails requestDetails = new NsiRequestDetailsFactory().create();

    when(virtualPortServiceMock.findByNsiV1StpId(anyString())).thenReturn(new VirtualPortFactory().setVirtualGroupAdminGroup("admin").create());
    thrown.expect(ValidationException.class);
    thrown.expectMessage("ProviderNsa 'urn:ogf:network:unknown' is not accepted");

    subject.reserve(connection, requestDetails, false, userDetails);
  }

}