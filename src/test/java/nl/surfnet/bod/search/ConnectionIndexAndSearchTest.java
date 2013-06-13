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
package nl.surfnet.bod.search;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.List;

import nl.surfnet.bod.domain.Connection;
import nl.surfnet.bod.domain.ConnectionV1;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.support.ConnectionV1Factory;
import nl.surfnet.bod.support.ReservationFactory;

import org.apache.lucene.queryParser.ParseException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ogf.schemas.nsi._2011._10.connection.types.ConnectionStateType;


public class ConnectionIndexAndSearchTest extends AbstractIndexAndSearch<ConnectionV1> {

  public ConnectionIndexAndSearchTest() {
    super(ConnectionV1.class);
  }

  @Before
  public void setupSearchData() {
    Reservation reservation = new ReservationFactory().setStatus(ReservationStatus.FAILED).withNoIds().create();
    Connection connection = new ConnectionV1Factory()
      .setReservation(reservation)
      .setCurrentState(ConnectionStateType.TERMINATED)
      .withNoIds().create();

    persist(connection);
  }

  @Test
  public void findConnectionByItsCurrentState() throws ParseException {
    List<ConnectionV1> connections = searchFor("TERMINATED");

    assertThat(connections, hasSize(1));
  }

  @Test
  @Ignore("Can not search from connection to reservation, results in circulair reference")
  public void findConnectionByItsReservationStatus() throws ParseException {

    List<ConnectionV1> connections = searchFor("FAILED");

    assertThat(connections, hasSize(1));
  }

  private void persist(Connection connection) {
    persist(
      connection.getReservation().getSourcePort().getPhysicalResourceGroup().getInstitute(),
      connection.getReservation().getSourcePort().getPhysicalResourceGroup(),
      connection.getReservation().getSourcePort().getPhysicalPort(),
      connection.getReservation().getSourcePort().getVirtualResourceGroup(),
      connection.getReservation().getSourcePort(),
      connection.getReservation().getDestinationPort().getPhysicalResourceGroup().getInstitute(),
      connection.getReservation().getDestinationPort().getPhysicalResourceGroup(),
      connection.getReservation().getDestinationPort().getPhysicalPort(),
      connection.getReservation().getDestinationPort(),
      connection.getReservation(),
      connection);
  }

}