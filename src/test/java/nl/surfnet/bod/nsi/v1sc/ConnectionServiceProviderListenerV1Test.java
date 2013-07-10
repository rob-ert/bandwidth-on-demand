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

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;

import nl.surfnet.bod.domain.ConnectionV1;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.service.ReservationStatusChangeEvent;
import nl.surfnet.bod.support.ConnectionV1Factory;
import nl.surfnet.bod.support.NsiRequestDetailsFactory;
import nl.surfnet.bod.support.ReservationFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceProviderListenerV1Test {

  @InjectMocks private ConnectionServiceProviderListenerV1 subject;

  @Mock private ConnectionServiceRequesterV1 connectionServiceRequesterMock;
  @Mock private ReservationService reservationServiceMock;

  @Test
  public void absentNsiRequestDetailsShouldDoNothing() {
    Reservation reservation = new ReservationFactory().create();

    when(reservationServiceMock.find(anyLong())).thenReturn(reservation);

    ReservationStatusChangeEvent event =
        new ReservationStatusChangeEvent(ReservationStatus.REQUESTED, reservation);

    subject.onStatusChange(event);
  }

  @Test
  public void reserveFailedWithReason() {
    String failedReason = "No available bandwidth";
    NsiRequestDetails requestDetails = new NsiRequestDetailsFactory().create();

    ConnectionV1 connection = new ConnectionV1Factory().setCurrentState(ConnectionStateType.RESERVING).setReserveRequestDetails(requestDetails).create();
    Reservation reservation = new ReservationFactory()
      .setStatus(ReservationStatus.NOT_ACCEPTED)
      .setConnectionV1(connection)
      .setFailedReason(failedReason).create();
    ReservationStatusChangeEvent event = new ReservationStatusChangeEvent(ReservationStatus.REQUESTED, reservation);

    when(reservationServiceMock.find(reservation.getId())).thenReturn(reservation);

    subject.onStatusChange(event);

    verify(connectionServiceRequesterMock).reserveFailed(
        connection, requestDetails, Optional.of(failedReason));
  }

  @Test
  public void terminateFailed() {
    NsiRequestDetails requestDetails = new NsiRequestDetailsFactory().create();
    ConnectionV1 connection = new ConnectionV1Factory().setCurrentState(ConnectionStateType.TERMINATING).setTerminateRequestDetails(requestDetails).create();
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.FAILED).setConnectionV1(connection).create();
    ReservationStatusChangeEvent event = new ReservationStatusChangeEvent(ReservationStatus.AUTO_START, reservation);

    when(reservationServiceMock.find(reservation.getId())).thenReturn(reservation);

    subject.onStatusChange(event);

    verify(connectionServiceRequesterMock).terminateFailed(connection, Optional.of(requestDetails));
  }

  @Test
  public void terminateSucceed() {
    NsiRequestDetails requestDetails = new NsiRequestDetailsFactory().create();
    ConnectionV1 connection = new ConnectionV1Factory().setCurrentState(ConnectionStateType.TERMINATING).setTerminateRequestDetails(requestDetails).create();
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.CANCELLED).setConnectionV1(connection).create();
    ReservationStatusChangeEvent event = new ReservationStatusChangeEvent(ReservationStatus.AUTO_START, reservation);

    when(reservationServiceMock.find(reservation.getId())).thenReturn(reservation);

    subject.onStatusChange(event);

    verify(connectionServiceRequesterMock).terminateConfirmed(connection, Optional.of(requestDetails));
  }

  @Test
  public void provisionSucceeded() {
    ConnectionV1 connection = new ConnectionV1Factory().setCurrentState(ConnectionStateType.RESERVED).create();
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.AUTO_START).setConnectionV1(connection).create();
    ReservationStatusChangeEvent event = new ReservationStatusChangeEvent(ReservationStatus.RESERVED, reservation);

    when(reservationServiceMock.find(reservation.getId())).thenReturn(reservation);

    subject.onStatusChange(event);

    verify(connectionServiceRequesterMock).provisionSucceeded(connection);
  }

  @Test
  public void reservationStarts() {
    NsiRequestDetails requestDetails = new NsiRequestDetailsFactory().create();
    ConnectionV1 connection = new ConnectionV1Factory().setCurrentState(ConnectionStateType.AUTO_PROVISION).setProvisionRequestDetails(requestDetails).create();
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.RUNNING).setConnectionV1(connection).create();
    ReservationStatusChangeEvent event = new ReservationStatusChangeEvent(ReservationStatus.AUTO_START, reservation);

    when(reservationServiceMock.find(reservation.getId())).thenReturn(reservation);

    subject.onStatusChange(event);

    verify(connectionServiceRequesterMock).provisionConfirmed(connection, requestDetails);
  }

  @Test
  public void succeedShouldTerminate() {
    ConnectionV1 connection = new ConnectionV1Factory().setCurrentState(ConnectionStateType.PROVISIONED).create();
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.SUCCEEDED).setConnectionV1(connection).create();
    ReservationStatusChangeEvent event = new ReservationStatusChangeEvent(ReservationStatus.RUNNING, reservation);

    when(reservationServiceMock.find(reservation.getId())).thenReturn(reservation);

    subject.onStatusChange(event);

    verify(connectionServiceRequesterMock).executionSucceeded(connection);
  }
}
