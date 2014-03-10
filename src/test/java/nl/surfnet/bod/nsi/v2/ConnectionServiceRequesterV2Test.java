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
package nl.surfnet.bod.nsi.v2;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.ogf.schemas.nsi._2013._12.connection.types.LifecycleStateEnumType.CREATED;
import static org.ogf.schemas.nsi._2013._12.connection.types.LifecycleStateEnumType.PASSED_END_TIME;
import static org.ogf.schemas.nsi._2013._12.connection.types.LifecycleStateEnumType.TERMINATED;
import static org.ogf.schemas.nsi._2013._12.connection.types.LifecycleStateEnumType.TERMINATING;
import static org.ogf.schemas.nsi._2013._12.connection.types.ProvisionStateEnumType.PROVISIONED;
import static org.ogf.schemas.nsi._2013._12.connection.types.ProvisionStateEnumType.PROVISIONING;
import static org.ogf.schemas.nsi._2013._12.connection.types.ProvisionStateEnumType.RELEASED;
import static org.ogf.schemas.nsi._2013._12.connection.types.ReservationStateEnumType.RESERVE_ABORTING;
import static org.ogf.schemas.nsi._2013._12.connection.types.ReservationStateEnumType.RESERVE_CHECKING;
import static org.ogf.schemas.nsi._2013._12.connection.types.ReservationStateEnumType.RESERVE_COMMITTING;
import static org.ogf.schemas.nsi._2013._12.connection.types.ReservationStateEnumType.RESERVE_FAILED;
import static org.ogf.schemas.nsi._2013._12.connection.types.ReservationStateEnumType.RESERVE_HELD;
import static org.ogf.schemas.nsi._2013._12.connection.types.ReservationStateEnumType.RESERVE_START;
import static org.ogf.schemas.nsi._2013._12.connection.types.ReservationStateEnumType.RESERVE_TIMEOUT;

import java.util.ArrayList;

import javax.xml.datatype.XMLGregorianCalendar;

import java.util.Optional;
import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.domain.NsiV2RequestDetails;
import nl.surfnet.bod.repo.ConnectionV2Repo;
import nl.surfnet.bod.support.ConnectionV2Factory;
import nl.surfnet.bod.support.NsiV2RequestDetailsFactory;
import nl.surfnet.bod.util.XmlUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ogf.schemas.nsi._2013._12.connection.types.DataPlaneStatusType;
import org.ogf.schemas.nsi._2013._12.connection.types.LifecycleStateEnumType;
import org.ogf.schemas.nsi._2013._12.framework.headers.CommonHeaderType;


@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceRequesterV2Test {

  @InjectMocks private ConnectionServiceRequesterV2 subject = new ConnectionServiceRequesterV2();

  @Mock private ConnectionV2Repo connectionRepMock;
  @Mock private ConnectionServiceRequesterClient requesterClientMock;

  @After
  public void tearDown() {
    DateTimeUtils.setCurrentMillisSystem();
  }

  @Test
  public void should_goto_reserve_held_when_sending_reserve_confirmed() {
    ConnectionV2 connection = new ConnectionV2Factory().setReservationState(RESERVE_CHECKING).create();

    when(connectionRepMock.findOne(1L)).thenReturn(connection);

    subject.reserveConfirmed(1L, new NsiV2RequestDetailsFactory().create());

    assertThat(connection.getReservationState(), is(RESERVE_HELD));
  }

  @Test
  public void should_set_reserve_held_timeout_when_going_to_reserve_held() {
    DateTime now = DateTime.now();
    DateTimeUtils.setCurrentMillisFixed(now.getMillis());
    ConnectionV2 connection = new ConnectionV2Factory().setReservationState(RESERVE_CHECKING).setReserveHeldTimeoutValue(100).create();
    assertThat(connection.getReserveHeldTimeout().isPresent(), is(false));

    when(connectionRepMock.findOne(1L)).thenReturn(connection);

    subject.reserveConfirmed(1L, new NsiV2RequestDetailsFactory().create());

    assertThat(connection.getReserveHeldTimeout(), is(Optional.of(now.plusSeconds(100))));
  }

  @Test
  public void should_goto_reserve_failed_when_sending_reserve_failed() {
    ConnectionV2 connection = new ConnectionV2Factory().setReservationState(RESERVE_CHECKING).create();

    when(connectionRepMock.findOne(1L)).thenReturn(connection);

    subject.reserveFailed(1L, new NsiV2RequestDetailsFactory().create());

    assertThat(connection.getReservationState(), is(RESERVE_FAILED));
    assertThat(connection.getProvisionState(), is(RELEASED));
  }

  @Test
  public void should_goto_reserve_start_when_sending_reserve_abort_confirm() {
    ConnectionV2 connection = new ConnectionV2Factory().setReservationState(RESERVE_ABORTING).create();

    when(connectionRepMock.findOne(1L)).thenReturn(connection);

    subject.reserveAbortConfirmed(1L, new NsiV2RequestDetailsFactory().create());

    assertThat(connection.getReservationState(), is(RESERVE_START));
    assertThat(connection.getProvisionState(), is(RELEASED));
    assertThat(connection.getDataPlaneActive(), is(false));
  }

  @Test
  public void should_goto_reserve_start_when_sending_reserve_commit_confirm() {
    ConnectionV2 connection = new ConnectionV2Factory().setReservationState(RESERVE_COMMITTING).create();

    when(connectionRepMock.findOne(1L)).thenReturn(connection);

    subject.reserveCommitConfirmed(1L, new NsiV2RequestDetailsFactory().create());

    assertThat(connection.getReservationState(), is(RESERVE_START));
    assertThat(connection.getLifecycleState(), is(CREATED));
    assertThat(connection.getProvisionState(), is(RELEASED));
    assertThat(connection.getDataPlaneActive(), is(false));
  }

  @Test
  public void should_goto_provisioned_when_sending_provision_confirmed() {
    ConnectionV2 connection = new ConnectionV2Factory().setProvisionState(PROVISIONING).create();

    when(connectionRepMock.findOne(1L)).thenReturn(connection);

    subject.provisionConfirmed(1L, new NsiV2RequestDetailsFactory().create());

    assertThat(connection.getProvisionState(), is(PROVISIONED));
  }

  @Test
  public void should_activate_the_data_plane_status() {
    ConnectionV2 connection = new ConnectionV2Factory().setDataPlaneActive(false).create();
    NsiV2RequestDetails requestDetails = new NsiV2RequestDetailsFactory().create();

    when(connectionRepMock.findOne(1L)).thenReturn(connection);
    subject.dataPlaneActivated(1L, requestDetails);

    ArgumentCaptor<CommonHeaderType> header = ArgumentCaptor.forClass(CommonHeaderType.class);
    ArgumentCaptor<DataPlaneStatusType> status = ArgumentCaptor.forClass(DataPlaneStatusType.class);
    verify(requesterClientMock).notifyDataPlaneStateChange(header.capture(), eq(connection.getConnectionId()), anyInt(), status.capture(), any(XMLGregorianCalendar.class), eq(requestDetails.getReplyTo()));
    assertThat(connection.getDataPlaneActive(), is(true));
    assertThat(header.getValue().getCorrelationId(), not(requestDetails.getCorrelationId()));
    assertThat(status.getValue().isActive(), is(true));
  }

  @Test
  public void should_deactivate_the_data_plane_status() {
    ConnectionV2 connection = new ConnectionV2Factory().setDataPlaneActive(true).create();

    when(connectionRepMock.findOne(1L)).thenReturn(connection);

    subject.dataPlaneDeactivated(1L, new NsiV2RequestDetailsFactory().create());

    assertThat(connection.getDataPlaneActive(), is(false));
  }

  @Test
  public void should_goto_terminated_when_sending_terminate_confirm() {
    ConnectionV2 connection = new ConnectionV2Factory().setLifecycleState(TERMINATING).create();

    when(connectionRepMock.findOne(1L)).thenReturn(connection);

    subject.terminateConfirmed(1L, new NsiV2RequestDetailsFactory().create());

    assertThat(connection.getLifecycleState(), is(TERMINATED));
  }

  @Test
  public void should_send_queryResultConfirmed_even_if_no_connection_was_found() {

    NsiV2RequestDetails nsiRequestDetails = new NsiV2RequestDetailsFactory().create();

    subject.querySummaryConfirmed(new ArrayList<ConnectionV2>(), nsiRequestDetails);
  }

  @Test
  public void should_goto_reserve_timeout_when_connection_times_out() {
    DateTime now = DateTime.now();
    ConnectionV2 connection = new ConnectionV2Factory().setReservationState(RESERVE_HELD).setReserveHeldTimeoutValue(100).create();
    when(connectionRepMock.findOne(1L)).thenReturn(connection);
    NsiV2RequestDetails requestDetails = new NsiV2RequestDetailsFactory().create();

    subject.reserveTimeout(1L, now);

    assertThat(connection.getReservationState(), is(RESERVE_TIMEOUT));
    ArgumentCaptor<CommonHeaderType> header = ArgumentCaptor.forClass(CommonHeaderType.class);
    verify(requesterClientMock).notifyReserveTimeout(header.capture(), eq(connection.getConnectionId()), eq(1L), eq(100), eq(XmlUtils.toGregorianCalendar(now)), eq(requestDetails.getReplyTo()));
  }

  @Test
  public void should_goto_passed_end_time_when_connection_is_passed_end_time() {
    ConnectionV2 connection = new ConnectionV2Factory().setReservationState(RESERVE_START).setLifecycleState(LifecycleStateEnumType.CREATED).create();
    when(connectionRepMock.findOne(1L)).thenReturn(connection);

    subject.reservePassedEndTime(1L);

    assertThat(connection.getLifecycleState(), is(PASSED_END_TIME));
    verifyNoMoreInteractions(requesterClientMock);
  }
}
