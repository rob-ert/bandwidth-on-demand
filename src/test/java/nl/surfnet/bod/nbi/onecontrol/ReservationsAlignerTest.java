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
package nl.surfnet.bod.nbi.onecontrol;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.persistence.NoResultException;

import com.google.common.base.Optional;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.support.ConnectionV2Factory;
import nl.surfnet.bod.support.ReservationFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReservationsAlignerTest {

  @InjectMocks
  private ReservationsAligner subject;

  @Mock
  private ReservationService reservationService;

  @Mock
  private NbiOneControlClient nbiOneControlClient;

  @Test
  public void poison_pill_returns_false() {
    subject.add(ReservationsAligner.POISON_PILL);
    assertFalse(subject.doAlign());
  }

  @Test
  public void reservation_is_set_to_lost_when_onecontrol_does_not_know_about_it() {
    final String unknownId = "unknown";
    subject.add(unknownId);
    when(nbiOneControlClient.getReservationStatus(unknownId)).thenReturn(Optional.<ReservationStatus> absent());

    assertTrue(subject.doAlign());

    verify(nbiOneControlClient, times(1)).getReservationStatus(unknownId);
    verify(reservationService, times(1)).updateStatus(unknownId, ReservationStatus.LOST);
    verifyNoMoreInteractions(reservationService);
  }

  @Test
  public void should_ignore_unknown_reservation_ids() {
    final String knownId = "known";
    ReservationStatus reserved = ReservationStatus.RESERVED;

    subject.add(knownId);
    assertTrue(subject.doAlign());

    when(nbiOneControlClient.getReservationStatus(knownId)).thenReturn(Optional.of(reserved));
    when(reservationService.updateStatus(knownId, reserved)).thenThrow(new NoResultException());
  }

  @Test
  public void should_not_crash_when_onecontrol_client_causes_an_exception() {
    final String knownId = "known";

    subject.add(knownId);

    when(nbiOneControlClient.getReservationStatus(knownId)).thenThrow(new RuntimeException("try to crash the aligner"));
    assertTrue(subject.doAlign());
  }

  @Test
  public void should_align_reservation_status_with_status_from_one_control() {
    final String knownId = "known";
    ReservationStatus oneControlStatus = ReservationStatus.SCHEDULED;
    subject.add(knownId);
    Reservation reservation = new ReservationFactory().create();

    when(nbiOneControlClient.getReservationStatus(knownId)).thenReturn(Optional.of(oneControlStatus));
    when(reservationService.updateStatus(knownId, oneControlStatus)).thenReturn(reservation);

    assertTrue(subject.doAlign());
    verify(reservationService, times(1)).updateStatus(knownId, oneControlStatus);
    verify(reservationService, never()).provision(reservation);
  }

  @Test
  public void should_automatically_provision_new_reservation_when_not_nsi_created() {
    final String knownId = "known";
    ReservationStatus oneControlStatus = ReservationStatus.RESERVED;
    subject.add(knownId);
    Reservation reservation = new ReservationFactory().create();
    when(nbiOneControlClient.getReservationStatus(knownId)).thenReturn(Optional.of(oneControlStatus));
    when(reservationService.updateStatus(knownId, oneControlStatus)).thenReturn(reservation);

    assertTrue(subject.doAlign());

    verify(reservationService, times(1)).provision(reservation);
  }

  @Test
  public void should_not_provision_new_reservation_when_nsi_created() {
    final String knownId = "known";
    ReservationStatus oneControlStatus = ReservationStatus.RESERVED;
    subject.add(knownId);
    Reservation reservation = new ReservationFactory().setConnectionV2(new ConnectionV2Factory().create()).create();

    when(nbiOneControlClient.getReservationStatus(knownId)).thenReturn(Optional.of(oneControlStatus));
    when(reservationService.updateStatus(knownId, oneControlStatus)).thenReturn(reservation);

    assertTrue(subject.doAlign());

    verify(reservationService, never()).provision(reservation);
  }
}
