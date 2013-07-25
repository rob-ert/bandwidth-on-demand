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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import nl.surfnet.bod.domain.ConnectionV1;
import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.service.ReservationStatusChangeEvent;
import nl.surfnet.bod.support.ConnectionV1Factory;
import nl.surfnet.bod.support.ConnectionV2Factory;
import nl.surfnet.bod.support.NsiRequestDetailsFactory;
import nl.surfnet.bod.support.ReservationFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ogf.schemas.nsi._2013._04.connection.types.LifecycleStateEnumType;
import org.ogf.schemas.nsi._2013._04.connection.types.ProvisionStateEnumType;
import org.ogf.schemas.nsi._2013._04.connection.types.ReservationStateEnumType;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceProviderListenerV2Test {

  @InjectMocks private ConnectionServiceProviderListenerV2 subject;

  @Mock private ConnectionServiceRequesterV2 requesterMock;

  @Test
  public void should_ignore_reservation_created_thourgh_gui() {
    Reservation reservation = new ReservationFactory().setConnectionV2(null).setConnectionV1(null).create();

    ReservationStatusChangeEvent event = new ReservationStatusChangeEvent(reservation, ReservationStatus.REQUESTED, reservation.getStatus());

    subject.onStatusChange(event);

    verifyZeroInteractions(requesterMock);
    verifyNoMoreInteractions(requesterMock);
  }

  @Test
  public void should_ignore_reservation_created_thourgh_by_nsiv1() {
    ConnectionV1 connection = new ConnectionV1Factory().create();
    Reservation reservation = new ReservationFactory().setConnectionV2(null).setConnectionV1(connection).create();

    ReservationStatusChangeEvent event = new ReservationStatusChangeEvent(reservation, ReservationStatus.REQUESTED, reservation.getStatus());

    subject.onStatusChange(event);

    verifyZeroInteractions(requesterMock);
    verifyNoMoreInteractions(requesterMock);
  }

  @Test
  public void should_send_reserve_confirmed() {
    ConnectionV2 connection = new ConnectionV2Factory()
      .setReservationState(ReservationStateEnumType.RESERVE_CHECKING).create();
    Reservation reservation = new ReservationFactory().setConnectionV2(connection)
      .setStatus(ReservationStatus.RESERVED).create();
    NsiRequestDetails requestDetails = new NsiRequestDetailsFactory().create();
    connection.setLastReservationRequestDetails(requestDetails);

    ReservationStatusChangeEvent event = new ReservationStatusChangeEvent(reservation, ReservationStatus.REQUESTED, reservation.getStatus());

    subject.onStatusChange(event);

    verify(requesterMock).reserveConfirmed(connection.getId(), requestDetails);
    verifyNoMoreInteractions(requesterMock);
  }

  @Test
  public void should_send_reserve_failed_when_not_accepted() {
    ConnectionV2 connection = new ConnectionV2Factory()
      .setReservationState(ReservationStateEnumType.RESERVE_CHECKING).create();
    Reservation reservation = new ReservationFactory().setConnectionV2(connection)
      .setStatus(ReservationStatus.NOT_ACCEPTED).create();
    NsiRequestDetails requestDetails = new NsiRequestDetailsFactory().create();
    connection.setLastReservationRequestDetails(requestDetails);

    ReservationStatusChangeEvent event = new ReservationStatusChangeEvent(reservation, ReservationStatus.REQUESTED, reservation.getStatus());

    subject.onStatusChange(event);

    verify(requesterMock).reserveFailed(connection.getId(), requestDetails);
    verifyNoMoreInteractions(requesterMock);
  }

  @Test
  public void should_send_reserve_failed_when_failed() {
    ConnectionV2 connection = new ConnectionV2Factory()
      .setReservationState(ReservationStateEnumType.RESERVE_CHECKING).create();
    Reservation reservation = new ReservationFactory().setConnectionV2(connection)
      .setStatus(ReservationStatus.FAILED).create();
    NsiRequestDetails requestDetails = new NsiRequestDetailsFactory().create();
    connection.setLastReservationRequestDetails(requestDetails);

    ReservationStatusChangeEvent event = new ReservationStatusChangeEvent(reservation, ReservationStatus.REQUESTED, reservation.getStatus());

    subject.onStatusChange(event);

    verify(requesterMock).reserveFailed(connection.getId(), requestDetails);
    verifyNoMoreInteractions(requesterMock);
  }

  @Test
  public void should_send_reserve_abort_confirmed_when_reservation_status_was_reserved() {
    ConnectionV2 connection = new ConnectionV2Factory()
      .setReservationState(ReservationStateEnumType.RESERVE_ABORTING)
      .setProvisionState(null)
      .setLifecycleState(null)
      .setDataPlaneActive(false).create();
    Reservation reservation = new ReservationFactory().setConnectionV2(connection)
        .setStatus(ReservationStatus.CANCELLED).create();
    NsiRequestDetails requestDetails = new NsiRequestDetailsFactory().create();
    connection.setLastReservationRequestDetails(requestDetails);

    ReservationStatusChangeEvent event = new ReservationStatusChangeEvent(reservation, ReservationStatus.RESERVED, reservation.getStatus());

    subject.onStatusChange(event);

    verify(requesterMock).reserveAbortConfirmed(connection.getId(), requestDetails);
    verifyNoMoreInteractions(requesterMock);
  }

  @Test
  public void should_send_reserve_abort_confirmed_when_reservation_status_was_scheduled() {
    ConnectionV2 connection = new ConnectionV2Factory()
      .setReservationState(ReservationStateEnumType.RESERVE_ABORTING)
      .setProvisionState(null)
      .setLifecycleState(null)
      .setDataPlaneActive(false).create();
    Reservation reservation = new ReservationFactory().setConnectionV2(connection)
        .setStatus(ReservationStatus.CANCELLED).create();
    NsiRequestDetails requestDetails = new NsiRequestDetailsFactory().create();
    connection.setLastReservationRequestDetails(requestDetails);

    ReservationStatusChangeEvent event = new ReservationStatusChangeEvent(reservation, ReservationStatus.SCHEDULED, reservation.getStatus());

    subject.onStatusChange(event);

    verify(requesterMock).reserveAbortConfirmed(connection.getId(), requestDetails);
    verifyNoMoreInteractions(requesterMock);
  }

  @Test
  public void should_send_provision_confirmed() {
    ConnectionV2 connection = new ConnectionV2Factory()
      .setReservationState(ReservationStateEnumType.RESERVE_START)
      .setProvisionState(ProvisionStateEnumType.RELEASED).create();
    Reservation reservation = new ReservationFactory().setConnectionV2(connection).setStatus(ReservationStatus.AUTO_START).create();
    NsiRequestDetails requestDetails = new NsiRequestDetailsFactory().create();
    connection.setLastProvisionRequestDetails(requestDetails);
    ReservationStatusChangeEvent event = new ReservationStatusChangeEvent(reservation, ReservationStatus.RESERVED, reservation.getStatus());

    subject.onStatusChange(event);

    verify(requesterMock).provisionConfirmed(connection.getId(), requestDetails);
    verifyNoMoreInteractions(requesterMock);
  }

  @Test
  public void should_send_data_plane_activated() {
    ConnectionV2 connection = new ConnectionV2Factory()
      .setReservationState(ReservationStateEnumType.RESERVE_START)
      .setProvisionState(ProvisionStateEnumType.PROVISIONED)
      .setDataPlaneActive(false).create();
    Reservation reservation = new ReservationFactory().setConnectionV2(connection).setStatus(ReservationStatus.RUNNING).create();

    ReservationStatusChangeEvent event = new ReservationStatusChangeEvent(reservation, ReservationStatus.AUTO_START, reservation.getStatus());

    subject.onStatusChange(event);

    verify(requesterMock).dataPlaneActivated(connection.getId(), connection.getInitialReserveRequestDetails());
    verifyNoMoreInteractions(requesterMock);
  }

  @Test
  public void should_send_data_plane_deactivated() {
    ConnectionV2 connection = new ConnectionV2Factory()
      .setReservationState(ReservationStateEnumType.RESERVE_START)
      .setProvisionState(ProvisionStateEnumType.PROVISIONED)
      .setDataPlaneActive(true).create();
    Reservation reservation = new ReservationFactory().setConnectionV2(connection).setStatus(ReservationStatus.SUCCEEDED).create();

    ReservationStatusChangeEvent event = new ReservationStatusChangeEvent(reservation, ReservationStatus.RUNNING, reservation.getStatus());

    subject.onStatusChange(event);

    verify(requesterMock).dataPlaneDeactivated(connection.getId(), connection.getInitialReserveRequestDetails());
    verifyNoMoreInteractions(requesterMock);
  }

  @Test
  public void should_send_data_plane_error() {
    ConnectionV2 connection = new ConnectionV2Factory()
      .setReservationState(ReservationStateEnumType.RESERVE_START)
      .setProvisionState(ProvisionStateEnumType.PROVISIONED)
      .setLifecycleState(LifecycleStateEnumType.CREATED)
      .setDataPlaneActive(true).create();
    Reservation reservation = new ReservationFactory().setConnectionV2(connection)
        .setStatus(ReservationStatus.FAILED).create();

    ReservationStatusChangeEvent event = new ReservationStatusChangeEvent(reservation, ReservationStatus.RUNNING, reservation.getStatus());

    subject.onStatusChange(event);

    verify(requesterMock).dataPlaneError(connection.getId(), connection.getInitialReserveRequestDetails());
    verifyNoMoreInteractions(requesterMock);
  }

  @Test
  public void should_send_terminate_confirmed_and_dataplane_deactivated_when_connection_was_active() {
    ConnectionV2 connection = new ConnectionV2Factory()
      .setReservationState(ReservationStateEnumType.RESERVE_START)
      .setProvisionState(ProvisionStateEnumType.PROVISIONED)
      .setLifecycleState(LifecycleStateEnumType.TERMINATING)
      .setDataPlaneActive(true).create();
    Reservation reservation = new ReservationFactory().setConnectionV2(connection)
        .setStatus(ReservationStatus.CANCELLED).create();
    NsiRequestDetails initialRequestDetails = new NsiRequestDetailsFactory().create();
    NsiRequestDetails terminateRequestDetails = new NsiRequestDetailsFactory().create();
    connection.setInitialReserveRequestDetails(initialRequestDetails);
    connection.setLastLifecycleRequestDetails(terminateRequestDetails);

    ReservationStatusChangeEvent event = new ReservationStatusChangeEvent(reservation, ReservationStatus.RUNNING, reservation.getStatus());

    subject.onStatusChange(event);

    verify(requesterMock).terminateConfirmed(connection.getId(), terminateRequestDetails);
    verify(requesterMock).dataPlaneDeactivated(connection.getId(), initialRequestDetails);
    verifyNoMoreInteractions(requesterMock);
  }

  @Test
  public void should_send_terminate_confirmed(){
    ConnectionV2 connection = new ConnectionV2Factory()
        .setReservationState(ReservationStateEnumType.RESERVE_START)
        .setProvisionState(ProvisionStateEnumType.PROVISIONED)
        .setLifecycleState(LifecycleStateEnumType.TERMINATING)
        .setDataPlaneActive(false).create();
    Reservation reservation = new ReservationFactory().setConnectionV2(connection)
        .setStatus(ReservationStatus.CANCELLED).create();
    NsiRequestDetails requestDetails = new NsiRequestDetailsFactory().create();
    connection.setLastLifecycleRequestDetails(requestDetails);

    ReservationStatusChangeEvent event = new ReservationStatusChangeEvent(reservation, ReservationStatus.RUNNING, reservation.getStatus());

    subject.onStatusChange(event);

    verify(requesterMock).terminateConfirmed(connection.getId(), requestDetails);
    verifyNoMoreInteractions(requesterMock);
  }

  @Test
  public void should_update_connection_lifecycle_state_machine() {
    ConnectionV2 connection = new ConnectionV2Factory()
      .setReservationState(ReservationStateEnumType.RESERVE_START)
      .setProvisionState(ProvisionStateEnumType.RELEASED)
      .setLifecycleState(LifecycleStateEnumType.CREATED)
      .setDataPlaneActive(true).create();
    Reservation reservation = new ReservationFactory().setConnectionV2(connection)
        .setStatus(ReservationStatus.PASSED_END_TIME).create();

    ReservationStatusChangeEvent event = new ReservationStatusChangeEvent(reservation, ReservationStatus.SCHEDULED, reservation.getStatus());

    subject.onStatusChange(event);

    verify(requesterMock).reservePassedEndTime(connection.getId());
  }

  @Test
  public void should_send_nothing_when_scheduled() {
    ConnectionV2 connection = new ConnectionV2Factory()
      .setReservationState(ReservationStateEnumType.RESERVE_START)
      .setProvisionState(ProvisionStateEnumType.RELEASED)
      .setLifecycleState(LifecycleStateEnumType.CREATED)
      .setDataPlaneActive(true).create();
    Reservation reservation = new ReservationFactory().setConnectionV2(connection)
        .setStatus(ReservationStatus.SCHEDULED).create();

    ReservationStatusChangeEvent event = new ReservationStatusChangeEvent(reservation, ReservationStatus.RESERVED, reservation.getStatus());

    subject.onStatusChange(event);

    verifyZeroInteractions(requesterMock);
  }

}
