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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import nl.surfnet.bod.domain.ConnectionV2;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.repo.ConnectionV2Repo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ogf.schemas.nsi._2013._07.connection.types.ReservationStateEnumType;


@RunWith(MockitoJUnitRunner.class)
public class ConnectionV2ReserveTimeoutPollerTest {

  @Mock private ConnectionV2Repo connectionV2Repo;
  @Mock private ConnectionServiceV2 connectionServiceV2;

  @InjectMocks private ConnectionV2ReserveTimeoutPoller subject = new ConnectionV2ReserveTimeoutPoller();

  private DateTime now = DateTime.now();
  private ConnectionV2 connectionV2;

  @Before
  public void setUp() {
    DateTimeUtils.setCurrentMillisFixed(now.getMillis());

    connectionV2 = new ConnectionV2();
    Reservation reservation = new Reservation();

    reservation.setId(1L);
    connectionV2.setReservation(reservation);
    connectionV2.setId(42L);
  }

  @After
  public void tearDown() {
    DateTimeUtils.setCurrentMillisSystem();
  }

  @Test
  public void should_query_for_timed_out_reserve_held_connections_using_current_time() {
    subject.timeOutUncommittedReservations();

    verify(connectionV2Repo).findByReservationStateAndReserveHeldTimeoutBefore(ReservationStateEnumType.RESERVE_HELD, now);
  }

  @Test
  public void should_time_out_reserve_held_connections() {

    when(connectionV2Repo.findByReservationStateAndReserveHeldTimeoutBefore(eq(ReservationStateEnumType.RESERVE_HELD), any(DateTime.class))).thenReturn(Collections.singletonList(connectionV2));

    subject.timeOutUncommittedReservations();

    verify(connectionServiceV2).asyncReserveTimeout(now, 1L, 42L);
  }
}
