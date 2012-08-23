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
package nl.surfnet.bod.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import nl.surfnet.bod.domain.NsiRequestDetails;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.nbi.NbiClient;
import nl.surfnet.bod.repo.ReservationRepo;
import nl.surfnet.bod.support.ReservationFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Optional;

@RunWith(MockitoJUnitRunner.class)
public class ReservationToNbiTest {

  @InjectMocks
  private ReservationToNbi subject;

  @Mock
  private NbiClient nbiClientMock;

  @Mock
  private ReservationRepo reservationRepoMock;

  @Mock
  private LogEventService logEeventServiceMock;

  @Mock
  private ReservationEventPublisher reservationEventPublisherMock;

  @Test
  public void terminateAReservation() {
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.RESERVED).setCancelReason(null).create();

    subject.terminate(reservation, "Cancelled by Truus", Optional.<NsiRequestDetails>absent());

    assertThat(reservation.getStatus(), is(ReservationStatus.CANCELLED));
    assertThat(reservation.getCancelReason(), is("Cancelled by Truus"));

    verify(nbiClientMock).cancelReservation(reservation.getReservationId());
    verify(reservationRepoMock).save(reservation);
    verify(reservationEventPublisherMock).notifyListeners(any(ReservationStatusChangeEvent.class));
  }

  @Test
  public void provisionSuccessShouldPublishChangeEvent() {
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.RESERVED).create();

    when(nbiClientMock.activateReservation(reservation.getReservationId())).thenReturn(true);

    subject.provision(reservation, Optional.<NsiRequestDetails>absent());

    verify(reservationRepoMock).save(reservation);
    verify(reservationEventPublisherMock).notifyListeners(any(ReservationStatusChangeEvent.class));
  }

  @Test
  public void provisionFailShouldNotPublishChangeEvent() {
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.RESERVED).create();

    when(nbiClientMock.activateReservation(reservation.getReservationId())).thenReturn(false);

    subject.provision(reservation, Optional.<NsiRequestDetails>absent());

    verifyZeroInteractions(reservationEventPublisherMock, reservationRepoMock);
  }

}
