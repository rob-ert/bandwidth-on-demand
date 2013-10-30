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
package nl.surfnet.bod.service;

import static nl.surfnet.bod.domain.ReservationStatus.AUTO_START;
import static nl.surfnet.bod.domain.ReservationStatus.CANCELLING;
import static nl.surfnet.bod.domain.ReservationStatus.RESERVED;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import nl.surfnet.bod.domain.ReservationStatus;

import com.google.common.base.Optional;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.UpdatedReservationStatus;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.repo.ReservationRepo;
import nl.surfnet.bod.support.ReservationFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReservationToNbiTest {

  @InjectMocks
  private ReservationToNbi subject;

  @Mock
  private NbiClient nbiClientMock;

  @Mock
  private ReservationRepo reservationRepoMock;

  @Mock
  private ReservationService reservationService;

  @Test
  public void terminateAReservation() {
    Reservation reservation = new ReservationFactory().setStatus(CANCELLING).setCancelReason(null).create();

    when(reservationRepoMock.findOne(reservation.getId())).thenReturn(reservation);
    when(reservationRepoMock.save(reservation)).thenReturn(reservation);
    when(nbiClientMock.cancelReservation(reservation.getReservationId())).thenReturn(Optional.<String>absent());

    subject.asyncTerminate(reservation.getId());

    verify(nbiClientMock).cancelReservation(reservation.getReservationId());
    verify(reservationService).updateStatus(reservation.getReservationId(), UpdatedReservationStatus.forNewStatus(ReservationStatus.CANCELLED));
  }

  @Test
  public void provisionSuccessShouldPublishChangeEvent() {
    Reservation reservation = new ReservationFactory().setStatus(RESERVED).create();

    when(reservationRepoMock.findOne(reservation.getId())).thenReturn(reservation);
    when(reservationRepoMock.save(reservation)).thenReturn(reservation);
    when(nbiClientMock.activateReservation(reservation.getReservationId())).thenReturn(true);

    subject.asyncProvision(reservation.getId());

    verify(reservationService).updateStatus(reservation.getReservationId(), UpdatedReservationStatus.forNewStatus(AUTO_START));
  }

  @Test
  public void provisionFailShouldNotPublishChangeEvent() {
    Reservation reservation = new ReservationFactory().setStatus(RESERVED).create();

    when(reservationRepoMock.findOne(reservation.getId())).thenReturn(reservation);
    when(nbiClientMock.activateReservation(reservation.getReservationId())).thenReturn(false);

    subject.asyncProvision(reservation.getId());

    verify(reservationRepoMock).findOne(reservation.getId());
    verifyNoMoreInteractions(reservationRepoMock, reservationService);
  }

}
