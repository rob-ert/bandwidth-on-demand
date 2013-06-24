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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.ConnectionV1;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.support.ConnectionV1Factory;
import nl.surfnet.bod.support.ReservationFactory;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Test;

public class ReservationArchiveTest {

  @Test
  public void testReservationArchive() throws Exception {
    final ObjectMapper mapper = ReservationService.mapper;

    assertThat(mapper.canSerialize(Reservation.class), is(true));

    final ConnectionV1 connection = new ConnectionV1Factory().create();
    final Reservation reservation = new ReservationFactory().setConnectionV1(connection).create();

    ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
    System.out.println(writer.writeValueAsString(reservation));

    final String reservationAsJson = mapper.writeValueAsString(reservation);

    final Reservation reservationFromJson = mapper.readValue(reservationAsJson, Reservation.class);
    final Connection connectionFromJson = reservationFromJson.getConnectionV1().get();

    assertThat(reservation.getStartDate(), is(reservationFromJson.getStartDate()));
    assertThat(reservation.getEndDate(), is(reservationFromJson.getEndDate()));
    assertThat(reservation.getDestinationPort().getAdminGroups(), is(reservationFromJson.getDestinationPort()
        .getAdminGroups()));
    assertThat(reservation.getDestinationPort().getNsiStpId(), is(reservationFromJson.getDestinationPort()
        .getNsiStpId()));

    assertThat(connection.getConnectionId(), is(connectionFromJson.getConnectionId()));
  }

}
