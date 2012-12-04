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
package nl.surfnet.bod.nsi.v1sc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.EnumSet;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.oauth.NsiScope;
import nl.surfnet.bod.nsi.v1sc.ConnectionServiceRequesterCallback;
import nl.surfnet.bod.repo.ConnectionRepo;
import nl.surfnet.bod.support.ConnectionFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.security.Security;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;

import com.sun.xml.ws.client.ClientTransportException;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceRequesterCallbackTest {

  @InjectMocks
  private ConnectionServiceRequesterCallback subject;

  @Mock
  private ConnectionRepo connectionRepoMock;

  private final Connection connection = new ConnectionFactory().setSourceStpId("Source Port").setDestinationStpId(
      "Destination Port").setProviderNSA("urn:provider").create();

  private final NsiRequestDetails request = new NsiRequestDetails("http://localhost:55446", "123456");

  @Test(expected = ClientTransportException.class)
  public void shouldTransferToScheduledWhenProvisionFailsAndStartTimeIsReached() {
    Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.of(NsiScope.PROVISION)).create());
    connection.setStartTime(DateTime.now().plusMinutes((1)));
    try {
      subject.provisionFailed(connection, request);
    }
    finally {
      assertThat(connection.getCurrentState(), is(ConnectionStateType.SCHEDULED));
    }
  }

  @Test(expected = ClientTransportException.class)
  public void shouldTransferToReservedWhenProvisionFailsAndStartTimeIsNotReached() {
    Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.of(NsiScope.PROVISION)).create());
    connection.setStartTime(DateTime.now().minusMinutes(1));
    try {
      subject.provisionFailed(connection, request);
    }
    finally {
      assertThat(connection.getCurrentState(), is(ConnectionStateType.RESERVED));
    }
  }

  @Test(expected = ClientTransportException.class)
  public void shouldTransferToReservedWhenProvisionFailsAndNoStartTimeIsPresent() {
    Security.setUserDetails(new RichUserDetailsFactory().setScopes(EnumSet.of(NsiScope.PROVISION)).create());

    connection.setStartTime(null);
    try {
      subject.provisionFailed(connection, request);
    }
    finally {
      assertThat(connection.getCurrentState(), is(ConnectionStateType.RESERVED));
    }
  }

}
