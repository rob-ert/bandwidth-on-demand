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
package nl.surfnet.bod.web.push;

import java.util.Locale;

import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.service.ReservationStatusChangeEvent;
import nl.surfnet.bod.support.ReservationFactory;

import org.junit.Test;
import org.springframework.context.MessageSource;

import com.google.common.base.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    assertThat(event.getGroupId(), is(reservation.getVirtualResourceGroup().getSurfconextGroupId()));
  }
}
