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
package nl.surfnet.bod.service;

import static nl.surfnet.bod.nsi.v1sc.ConnectionServiceProviderFunctions.RESERVE_REQUEST_TO_CONNECTION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType.PROVISIONED;
import static org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType.TERMINATED;

import java.util.EnumSet;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.nsi.v1sc.ConnectionServiceProviderWsTest;
import nl.surfnet.bod.repo.ConnectionRepo;
import nl.surfnet.bod.support.ConnectionFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ogf.schemas.nsi._2011._10.connection._interface.ReserveRequestType;
import org.ogf.schemas.nsi._2011._10.connection.provider.ServiceException;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryConfirmedType;
import org.ogf.schemas.nsi._2011._10.connection.types.QueryOperationType;

import com.google.common.base.Optional;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceTest {

  @Mock
  public ConnectionRepo connectionRepoMock;

  @Mock
  private ReservationService reservationServiceMock;

  @Mock
  private VirtualPortService virtualPortServiceMock;

  @InjectMocks
  private ConnectionService subject;

  @Test
  public void shouldUseRichUserDetailsAsCreater() throws ServiceException {
    RichUserDetails userDetails = new RichUserDetailsFactory().setUsername("me").create();
    Security.setUserDetails(userDetails);

    ReserveRequestType reserveRequest = ConnectionServiceProviderWsTest.createReservationRequestType(512, Optional
        .<String> absent());
    reserveRequest.getReserve().setProviderNSA("nsa:surfnet.nl");
    reserveRequest.getReserve().getReservation().setConnectionId("123");
    NsiRequestDetails nsiRequestDetails = new NsiRequestDetails("replyTo", "123");

    Connection connection = RESERVE_REQUEST_TO_CONNECTION.apply(reserveRequest);
    when(virtualPortServiceMock.findByNsiStpId(anyString())).thenReturn(new VirtualPortFactory().create());
    when(connectionRepoMock.saveAndFlush(any(Connection.class))).thenReturn(connection);

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
      assertThat(ConnectionService.STATE_MAPPING.get(status), notNullValue());
    }
  }

  @Test
  public void aConnectionWithoutAReservationShouldBeTerminated() {
    Connection connectionValid = new ConnectionFactory().setReservation(null).setCurrentState(TERMINATED).create();
    assertThat(subject.hasValidState(connectionValid), is(true));

    Connection connectionInvalid = new ConnectionFactory().setReservation(null).setCurrentState(PROVISIONED).create();
    assertThat(subject.hasValidState(connectionInvalid), is(false));
  }

}