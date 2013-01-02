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
package nl.surfnet.bod.web.push;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.service.ReservationStatusChangeEvent;
import nl.surfnet.bod.support.ReservationFactory;

import org.junit.Test;
import org.springframework.context.MessageSource;

import com.google.common.base.Optional;

public class PushMessagesTest {

  private final MessageSource messageSourceMock = mock(MessageSource.class);

  @Test
  public void aReservationStatusChangedEventShouldHaveAJsonMessage() {
    Reservation reservation = new ReservationFactory().setId(54L).setStatus(ReservationStatus.AUTO_START).create();

    ReservationStatusChangeEvent reservationStatusChangeEvent = new ReservationStatusChangeEvent(
        ReservationStatus.RUNNING, reservation, Optional.<NsiRequestDetails>absent());

    when(messageSourceMock.getMessage(eq("info_reservation_statuschanged"), any(Object[].class), any(Locale.class)))
        .thenReturn("Yes");

    PushMessage event = PushMessages.createMessage(reservationStatusChangeEvent, messageSourceMock);

    assertThat(event.getMessage(), containsString("\"id\":54"));
    assertThat(event.getMessage(), containsString("Yes"));
    assertThat(event.getGroupId(), is(reservation.getVirtualResourceGroup().getAdminGroup()));
  }
}
