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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.domain.NsiV2RequestDetails;
import nl.surfnet.bod.repo.ConnectionV2Repo;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.support.ConnectionV2Factory;
import nl.surfnet.bod.support.NsiV2RequestDetailsFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.web.security.RichUserDetails;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ogf.schemas.nsi._2013._12.connection.types.DataPlaneStateChangeRequestType;
import org.ogf.schemas.nsi._2013._12.connection.types.ErrorEventType;
import org.ogf.schemas.nsi._2013._12.connection.types.NotificationBaseType;
import org.ogf.schemas.nsi._2013._12.connection.types.ReservationStateEnumType;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceV2Test {

  @InjectMocks private ConnectionServiceV2 subject;

  @Mock private ConnectionV2Repo connectionRepoMock;
  @Mock private ReservationService reservationServiceMock;
  @Mock private ConnectionServiceRequesterV2 connectionServiceRequesterMock;

  @Test
  public void provision_should_set_the_last_provision_request_details() {
    NsiV2RequestDetails provisionRequestDetails = new NsiV2RequestDetailsFactory().create();
    ConnectionV2 connection = new ConnectionV2Factory().create();
    when(connectionRepoMock.findByConnectionId("ConnectionId")).thenReturn(connection);

    subject.asyncProvision("ConnectionId", provisionRequestDetails);

    verify(reservationServiceMock).provision(connection.getReservation());
    assertThat(connection.getLastProvisionRequestDetails(), is(provisionRequestDetails));
  }

  @Test
  public void terminate_should_set_the_last_lifecyle_request_details() {
    NsiV2RequestDetails terminateRequestDetails = new NsiV2RequestDetailsFactory().create();
    ConnectionV2 connection = new ConnectionV2Factory().create();
    RichUserDetails user = new RichUserDetailsFactory().create();
    when(connectionRepoMock.findByConnectionId("ConnectionId")).thenReturn(connection);

    subject.asyncTerminate("ConnectionId", terminateRequestDetails, user);

    verify(reservationServiceMock).cancelWithReason(eq(connection.getReservation()), any(String.class), eq(user));
    assertThat(connection.getLastLifecycleRequestDetails(), is(terminateRequestDetails));
  }

  @Test
  public void reserve_commit_should_set_the_last_reservation_request_details() {
    NsiV2RequestDetails reserveCommitRequestDetails = new NsiV2RequestDetailsFactory().create();
    ConnectionV2 connection = new ConnectionV2Factory().create();
    when(connectionRepoMock.findByConnectionId("ConnectionId")).thenReturn(connection);

    subject.asyncReserveCommit("ConnectionId", reserveCommitRequestDetails);

    verify(connectionServiceRequesterMock).reserveCommitConfirmed(connection.getId(), reserveCommitRequestDetails);
    assertThat(connection.getLastReservationRequestDetails(), is(reserveCommitRequestDetails));
  }

  @Test
  public void reserve_with_a_duplicate_global_reservation_id_should_give_validation_exception() throws ConnectionServiceV2.ReservationCreationException {
    String providerNsa = "nsa:surfnet.nl";
    ConnectionV2 connection = new ConnectionV2Factory().setGlobalReservationId("GlobalReservationId").setProviderNsa(providerNsa).create();
    NsiV2RequestDetails requestDetails = new NsiV2RequestDetailsFactory().create();
    RichUserDetails userDetails = new RichUserDetailsFactory().create();

    when(connectionRepoMock.findByGlobalReservationId("GlobalReservationId")).thenReturn(connection);

    try {
      subject.reserve(connection, requestDetails, userDetails);
      fail("Expected a ValidationException");
    } catch (ConnectionServiceV2.ReservationCreationException e) {
      assertThat(e.getMessage(), containsString("GlobalReservationId"));
    }

    verify(connectionRepoMock, never()).save(any(ConnectionV2.class));
  }

  @Test
  public void reserve_with_a_end_time_before_start_time_should_give_validation_exception() throws ConnectionServiceV2.ReservationCreationException {
    String providerNsa = "nsa:surfnet.nl";
    ConnectionV2 connection = new ConnectionV2Factory().setStartTime(DateTime.now().plusMinutes(20)).setEndTime(DateTime.now()).setProviderNsa(providerNsa).create();
    NsiV2RequestDetails requestDetails = new NsiV2RequestDetailsFactory().create();
    RichUserDetails userDetails = new RichUserDetailsFactory().create();

    try {
      subject.reserve(connection, requestDetails, userDetails);
      fail("Expected a ValidationException");
    } catch (ConnectionServiceV2.ReservationCreationException e) {
      assertThat(e.getMessage(), containsString("Start time is after end time"));
    }

    verify(connectionRepoMock, never()).save(any(ConnectionV2.class));
  }

  @Test
  public void reserveAbort_held_reservation_should_terminate_connection() {
    ConnectionV2 connection = new ConnectionV2Factory().setReservationState(ReservationStateEnumType.RESERVE_HELD).create();
    NsiV2RequestDetails requestDetails = new NsiV2RequestDetailsFactory().create();
    RichUserDetails userDetails = new RichUserDetailsFactory().create();
    when(connectionRepoMock.findByConnectionId(connection.getConnectionId())).thenReturn(connection);

    subject.asyncReserveAbort(connection.getConnectionId(), requestDetails, userDetails);

    assertThat(connection.getReservationState(), is(ReservationStateEnumType.RESERVE_ABORTING));
    verify(reservationServiceMock).cancelWithReason(eq(connection.getReservation()), any(String.class), eq(userDetails));
  }

  @Test
  public void reserveAbort_failed_reservation_should_confirm_reserve_abort() {
    reserveAbortWithState(ReservationStateEnumType.RESERVE_FAILED);
  }

  @Test
  public void reserveAbort_timed_out_reservation_should_confirm_reserve_abort() {
    reserveAbortWithState(ReservationStateEnumType.RESERVE_TIMEOUT);
  }

  private void reserveAbortWithState(ReservationStateEnumType reservationState) {
    ConnectionV2 connection = new ConnectionV2Factory().setReservationState(reservationState).create();
    NsiV2RequestDetails requestDetails = new NsiV2RequestDetailsFactory().create();
    RichUserDetails userDetails = new RichUserDetailsFactory().create();
    when(connectionRepoMock.findByConnectionId(connection.getConnectionId())).thenReturn(connection);

    subject.asyncReserveAbort(connection.getConnectionId(), requestDetails, userDetails);

    assertThat(connection.getReservationState(), is(ReservationStateEnumType.RESERVE_START));
    verify(connectionServiceRequesterMock).reserveAbortConfirmed(connection.getId(), requestDetails);
  }

  @Test
  public void querySummarySync_should_return_an_empty_list_when_no_connection_objects_where_found() {
    final String nonExistingConnectionId = "1";

    when(connectionRepoMock.findByConnectionId(nonExistingConnectionId)).thenReturn(null);

    List<ConnectionV2> connections = subject.querySummary(ImmutableList.of(nonExistingConnectionId), Collections.<String>emptyList(), "requesterNsa");

    assertThat(connections, hasSize(0));
  }

  @Test
  public void querySummarySync_should_query_by_connection_id() {
    ConnectionV2 connection = new ConnectionV2Factory().create();
    when(connectionRepoMock.findByConnectionId("connectionId")).thenReturn(connection);

    List<ConnectionV2> connections = subject.querySummary(ImmutableList.of("connectionId"), Collections.<String>emptyList(), "requesterNsa");

    assertThat(connections, contains(connection));
  }

  @Test
  public void querySummarySync_should_query_by_global_reservation_id() {
    ConnectionV2 connection = new ConnectionV2Factory().create();
    when(connectionRepoMock.findByGlobalReservationId("globalReservationId")).thenReturn(connection);

    List<ConnectionV2> connections = subject.querySummary(Collections.<String>emptyList(), ImmutableList.of("globalReservationId"), "requesterNsa");

    assertThat(connections, contains(connection));
  }

  @Test
  public void querySummarySync_should_query_by_requester_nsa_when_both_global_reservation_id_and_connection_id_are_empty() {
    ConnectionV2 connection = new ConnectionV2Factory().create();
    when(connectionRepoMock.findByRequesterNsa("requesterNsa")).thenReturn(ImmutableList.of(connection));

    List<ConnectionV2> connections = subject.querySummary(Collections.<String>emptyList(), Collections.<String>emptyList(), "requesterNsa");

    assertThat(connections, contains(connection));
  }

  @Test
  public void queryNotification_should_return_diffent_type_of_notifications() {
    final String connectionId = "f00f";
    final NsiV2RequestDetails requestDetails = new NsiV2RequestDetailsFactory().create();
    ConnectionV2 connection = new ConnectionV2Factory().create();
    connection.setConnectionId(connectionId);

    connection.addNotification(new ErrorEventType().withNotificationId(0));
    connection.addNotification(new DataPlaneStateChangeRequestType().withNotificationId(1));

    when(connectionRepoMock.findByConnectionId(connectionId)).thenReturn(connection);
    List<NotificationBaseType> notifications = subject.queryNotification(connectionId, Optional.<Long>absent(), Optional.<Long>absent(), requestDetails);

    assertThat(notifications, hasSize(2));
  }

  @Test
  public void queryNotification_ranges_are_optional() {
    final String connectionId = "f00f";
    final NsiV2RequestDetails requestDetails = new NsiV2RequestDetailsFactory().create();
    ConnectionV2 connection = new ConnectionV2Factory().create();
    connection.setConnectionId(connectionId);

    for (int i = 0; i < 4; i++){
      ErrorEventType notification = new ErrorEventType();
      notification.setNotificationId(i);
      connection.addNotification(notification);
    }

    when(connectionRepoMock.findByConnectionId(connectionId)).thenReturn(connection);
    List<NotificationBaseType> notifications = subject.queryNotification(connectionId, Optional.<Long>absent(), Optional.<Long>absent(), requestDetails);

    assertThat(notifications, hasSize(4));
  }

  @Test
  public void queryNotification_should_only_post_back_notifications_that_are_in_range() {
    final Optional<Long> lowerBound = Optional.of(2L);
    final Optional<Long> upperBound = Optional.of(3L);
    final String connectionId = "f00f";
    final NsiV2RequestDetails requestDetails = new NsiV2RequestDetailsFactory().create();
    ConnectionV2 connection = new ConnectionV2Factory().create();
    connection.setConnectionId(connectionId);

    for (long i = lowerBound.get() - 1; i <= upperBound.get() + 1; i++){
      ErrorEventType notification = new ErrorEventType();
      notification.setNotificationId(i);
      connection.addNotification(notification);
    }

    when(connectionRepoMock.findByConnectionId(connectionId)).thenReturn(connection);
    List<NotificationBaseType> notifications = subject.queryNotification(connectionId, lowerBound, upperBound, requestDetails);

    assertThat(notifications, hasSize(2));
  }

  @Test
  public void queryNotification_should_return_empty_list_when_connection_not_found(){
    final String connectionId = "f00f";
    final NsiV2RequestDetails requestDetails = new NsiV2RequestDetailsFactory().create();

    when(connectionRepoMock.findByConnectionId(connectionId)).thenReturn(null);
    List<NotificationBaseType> notifications = subject.queryNotification(connectionId, Optional.<Long>absent(), Optional.<Long>absent(), requestDetails);

    assertThat(notifications, empty());
  }

}