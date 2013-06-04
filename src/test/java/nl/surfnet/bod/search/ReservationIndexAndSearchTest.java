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
package nl.surfnet.bod.search;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.List;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.support.ConnectionV1Factory;
import nl.surfnet.bod.support.ReservationFactory;

import org.apache.lucene.queryParser.ParseException;
import org.junit.Test;

public class ReservationIndexAndSearchTest extends AbstractIndexAndSearch<Reservation>{

  public ReservationIndexAndSearchTest() {
    super(Reservation.class);
  }

  @Test
  public void searchAndFindReservationOnNsiConnectionId() throws ParseException {
    Connection connection = new ConnectionV1Factory().setConnectionId("123-abc-456-def").withNoIds().create();
    Reservation reservation = new ReservationFactory().setConnection(connection).withNoIds().create();

    persist(reservation);

    List<Reservation> reservations = searchFor("123-abc-456-def");

    assertThat(reservations, hasSize(1));
  }

  private void persist(Reservation reservation) {
    persist(
      reservation.getSourcePort().getPhysicalResourceGroup().getInstitute(),
      reservation.getSourcePort().getPhysicalResourceGroup(),
      reservation.getSourcePort().getPhysicalPort(),
      reservation.getSourcePort().getVirtualResourceGroup(),
      reservation.getSourcePort(),
      reservation.getDestinationPort().getPhysicalResourceGroup().getInstitute(),
      reservation.getDestinationPort().getPhysicalResourceGroup(),
      reservation.getDestinationPort().getPhysicalPort(),
      reservation.getDestinationPort(),
      reservation);
  }

}