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
package nl.surfnet.bod.nbi.mtosi;


import static nl.surfnet.bod.util.TestHelper.testProperties;

import java.util.concurrent.atomic.AtomicLong;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.util.TestHelper.PropertiesEnvironment;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

public class ServiceComponentActivationClientTestIntegration {

  private ServiceComponentActivationClient subject;

  @Before
  public void setup() {
    PropertiesEnvironment testEnv = testProperties();
    subject = new ServiceComponentActivationClient(testEnv.getProperty("nbi.mtosi.service.reserve.endpoint"));
    subject.setValueIncrementer(new DataFieldMaxValueIncrementer() {

      private final AtomicLong atomicLong = new AtomicLong();

      @Override
      public String nextStringValue() throws DataAccessException {
        return "" + atomicLong.getAndIncrement();
      }

      @Override
      public long nextLongValue() throws DataAccessException {
        return atomicLong.getAndIncrement();
      }

      @Override
      public int nextIntValue() throws DataAccessException {
        return (int) atomicLong.getAndIncrement();
      }
    });
  }

  @Test
  public void shouldReturnSapNotFound() {
    PhysicalPort sourcePort = createPort("SAP-00:03:18:58:ce:20-8", "00:03:18:58:ce:20", "1-1-1-8");
    PhysicalPort destPort = createPort("SAP-00:03:18:58:ce:20-4", "00:03:18:58:ce:20", "1-1-1-8");

    Reservation reservation = new ReservationFactory()
      .setReservationId("SURFnetTest2")
      .setStartDateTime(DateTime.now().plusMinutes(5))
      .setEndDateTime(DateTime.now().plusMinutes(35))
      .setName("mtosiSurfTest5")
      .create();

    reservation.getSourcePort().setPhysicalPort(sourcePort);
    reservation.getDestinationPort().setPhysicalPort(destPort);

    Reservation savedReservation = subject.reserve(reservation, false);

    System.err.println(savedReservation);
  }

  private PhysicalPort createPort(String name, String me, String ptp) {
    return new PhysicalPortFactory()
      .setNmsSapName(name)
      .setNmsPortId(me+"@"+ptp)
      .setNmsNeId(me)
      .create();
  }

  //00:03:18:58:ce:80-[5, 7]
  // SAP-00:03:18:58:ce:80-5 00:03:18:58:ce:80@1-1-1-5
  // SAP-00:03:18:58:ce:80-7 00:03:18:58:ce:80@1-1-1-7
  //00:03:18:58:ce:20-[1, 4, 5, 8]
  // SAP-00:03:18:58:ce:20-1 00:03:18:58:ce:20@1-1-1-1
  // SAP-00:03:18:58:ce:20-4 00:03:18:58:ce:20@1-1-1-4
  // SAP-00:03:18:58:ce:20-5 00:03:18:58:ce:20@1-1-1-5
  // SAP-00:03:18:58:ce:20-8 00:03:18:58:ce:20@1-1-1-8

}