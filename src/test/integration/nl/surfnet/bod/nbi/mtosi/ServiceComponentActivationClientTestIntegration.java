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
package nl.surfnet.bod.nbi.mtosi;


import static nl.surfnet.bod.util.TestHelper.mtosiProperties;
import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.util.TestHelper.PropertiesEnvironment;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ServiceComponentActivationClientTestIntegration {

  private ServiceComponentActivationClient subject;

  @Before
  public void setup() {
    PropertiesEnvironment testEnv = mtosiProperties();
    subject = new ServiceComponentActivationClient(testEnv.getProperty("nbi.mtosi.service.reserve.endpoint"));
  }

  @Test
  @Ignore
  public void shouldCreateReservation() {
//    PhysicalPort sourcePort = createPort("SAP-00:03:18:58:ce:20-8", "00:03:18:58:ce:20", "1-1-1-8");
//    PhysicalPort destPort = createPort("SAP-00:03:18:58:ce:20-4", "00:03:18:58:ce:20", "1-1-1-8");
    PhysicalPort sourcePort = createPort("00:03:18:f2:9a:30-3", "00:03:18:f2:9a:30", "1-1-1-3-1");
    PhysicalPort destPort = createPort("00:03:18:f2:9a:30-2", "00:03:18:f2:9a:30", "1-1-1-2-1");
//    PhysicalPort destPort = createPort("00:03:18:f2:9a:50-4", "00:03:18:f2:9a:50", "1-1-1-4");

    Reservation reservation = new ReservationFactory()
      .setReservationId("HansAlanTest7")
      .setStartDateTime(DateTime.now().plusMinutes(25))
      .setEndDateTime(DateTime.now().plusMinutes(45))
      .setName("HansAlanTest7")
      .setBandwidth(100)
      .withoutProtection()
      .create();

    reservation.getSourcePort().setPhysicalPort(sourcePort);
    reservation.getDestinationPort().setPhysicalPort(destPort);

    Reservation savedReservation = subject.reserve(reservation, false);

    System.err.println(savedReservation);
  }

  @Test
//  @Ignore
  public void shouldActivateReservation() {
    Reservation reservation = new ReservationFactory()
      .setReservationId("HansAlanTest6")
      .create();

    subject.activate(reservation);
  }

  @Test
  @Ignore
  public void shouldTerminateReservation() {
    Reservation reservation = new ReservationFactory()
      .setReservationId("HansAlanTest5")
      .create();

    subject.terminate(reservation);
  }

  private PhysicalPort createPort(String name, String me, String ptp) {
    return new PhysicalPortFactory()
      .setNmsSapName(name)
      .setNmsPortId(me+"@"+ptp)
      .setNmsNeId(me)
      .create();
  }

}