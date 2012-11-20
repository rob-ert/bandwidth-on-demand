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
package nl.surfnet.bod.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import nl.surfnet.bod.service.ReservationService;
import nl.surfnet.bod.support.ConnectionFactory;
import nl.surfnet.bod.support.ReservationFactory;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

public class ReservationArchiveTest {

  @Test
  public void testReservationArchive() throws Exception {
    final ObjectMapper mapper = new ReservationService().getObjectMapper();

    assertThat(mapper.canSerialize(Reservation.class), is(true));

    final Connection connection = new ConnectionFactory().create();
    final Reservation reservation = new ReservationFactory().setConnection(connection).create();

//    ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
//    System.out.println(writer.writeValueAsString(reservation));

    final String reservationAsJson = mapper.writeValueAsString(reservation);

    final Reservation reservationFromJson = mapper.readValue(reservationAsJson, Reservation.class);
    final Connection connectionFromJson = reservationFromJson.getConnection().get();

    assertThat(reservation.getStartDate(), is(reservationFromJson.getStartDate()));
    assertThat(reservation.getEndDate(), is(reservationFromJson.getEndDate()));
    assertThat(reservation.getDestinationPort().getAdminGroup(), is(reservationFromJson.getDestinationPort()
        .getAdminGroup()));
    assertThat(reservation.getDestinationPort().getNsiStpId(), is(reservationFromJson.getDestinationPort()
        .getNsiStpId()));

    assertThat(connection.getConnectionId(), is(connectionFromJson.getConnectionId()));
    assertThat(connection.getCurrentState(), is(connectionFromJson.getCurrentState()));
  }

}
