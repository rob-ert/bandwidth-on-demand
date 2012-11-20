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
