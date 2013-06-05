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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.ogf.schemas.nsi._2013._04.connection.types.LifecycleStateEnumType.TERMINATED;
import static org.ogf.schemas.nsi._2013._04.connection.types.LifecycleStateEnumType.TERMINATING;
import static org.ogf.schemas.nsi._2013._04.connection.types.ProvisionStateEnumType.PROVISIONED;
import static org.ogf.schemas.nsi._2013._04.connection.types.ProvisionStateEnumType.PROVISIONING;
import static org.ogf.schemas.nsi._2013._04.connection.types.ReservationStateEnumType.RESERVE_ABORTING;
import static org.ogf.schemas.nsi._2013._04.connection.types.ReservationStateEnumType.RESERVE_CHECKING;
import static org.ogf.schemas.nsi._2013._04.connection.types.ReservationStateEnumType.RESERVE_COMMITTING;
import static org.ogf.schemas.nsi._2013._04.connection.types.ReservationStateEnumType.RESERVE_HELD;
import static org.ogf.schemas.nsi._2013._04.connection.types.ReservationStateEnumType.RESERVE_START;

import java.util.ArrayList;

import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.repo.ConnectionV2Repo;
import nl.surfnet.bod.support.ConnectionV2Factory;
import nl.surfnet.bod.support.NsiRequestDetailsFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceRequesterV2Test {

  @InjectMocks private ConnectionServiceRequesterV2 subject = new ConnectionServiceRequesterV2();

  @Mock private ConnectionV2Repo connectionRepMock;

  @Test
  public void should_goto_reserve_held_when_sending_reserve_confirmed() {
    ConnectionV2 connection = new ConnectionV2Factory().setReservationState(RESERVE_CHECKING).create();

    when(connectionRepMock.findOne(1L)).thenReturn(connection);

    subject.reserveConfirmed(1L, new NsiRequestDetailsFactory().create());

    assertThat(connection.getReservationState(), is(RESERVE_HELD));
  }

  @Test
  public void should_goto_reserve_start_when_sending_reserve_abort_confirm() {
    ConnectionV2 connection = new ConnectionV2Factory().setReservationState(RESERVE_ABORTING).create();

    when(connectionRepMock.findOne(1L)).thenReturn(connection);

    subject.reserveAbortConfirmed(1L, new NsiRequestDetailsFactory().create());

    assertThat(connection.getReservationState(), is(RESERVE_START));
  }

  @Test
  public void should_goto_reserve_start_when_sending_reserve_commit_confirm() {
    ConnectionV2 connection = new ConnectionV2Factory().setReservationState(RESERVE_COMMITTING).create();

    when(connectionRepMock.findOne(1L)).thenReturn(connection);

    subject.reserveCommitConfirmed(1L, new NsiRequestDetailsFactory().create());

    assertThat(connection.getReservationState(), is(RESERVE_START));
  }

  @Test
  public void should_goto_provisioned_when_sending_provision_confirmed() {
    ConnectionV2 connection = new ConnectionV2Factory().setProvisionState(PROVISIONING).create();

    when(connectionRepMock.findOne(1L)).thenReturn(connection);

    subject.provisionConfirmed(1L, new NsiRequestDetailsFactory().create());

    assertThat(connection.getProvisionState(), is(PROVISIONED));
  }

  @Test
  public void should_activate_the_data_plane_status() {
    ConnectionV2 connection = new ConnectionV2Factory().setDataPlaneActive(false).create();

    when(connectionRepMock.findOne(1L)).thenReturn(connection);

    subject.dataPlaneActivated(1L, new NsiRequestDetailsFactory().create());

    assertThat(connection.isDataPlaneActive(), is(true));
  }

  @Test
  public void should_goto_terminated_when_sending_terminate_confirm() {
    ConnectionV2 connection = new ConnectionV2Factory().setLifecycleState(TERMINATING).create();

    when(connectionRepMock.findOne(1L)).thenReturn(connection);

    subject.terminateConfirmed(1L, new NsiRequestDetailsFactory().create());

    assertThat(connection.getLifecycleState(), is(TERMINATED));
  }

  @Test
  public void should_send_queryResultConfirmed_even_if_no_connection_was_found() {

    NsiRequestDetails nsiRequestDetails = new NsiRequestDetailsFactory().create();

    subject.querySummaryConfirmed(new ArrayList<ConnectionV2>(), nsiRequestDetails);
  }

}
