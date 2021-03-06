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
package nl.surfnet.bod.nbi.onecontrol;

import javax.xml.bind.Marshaller;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.UniPort;
import nl.surfnet.bod.domain.UpdatedReservationStatus;
import nl.surfnet.bod.support.NbiPortFactory;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.ReservationFactory;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ServiceComponentActivationClientTestIntegration {

  private ServiceComponentActivationClient subject;

  @Before
  public void setup() {
    subject = new ServiceComponentActivationClient();
  }

  @Test
  @Ignore
  public void shouldCreateReservation() {
    UniPort sourcePort = createPort("00:03:18:ec:21:40-1.3", "00:03:18:ec:21:40", "1-1-1-3-1");
    UniPort destPort = createPort("00:03:18:ec:21:40-1.4", "00:03:18:ec:21:40", "1-1-1-4-1");

    Reservation reservation = new ReservationFactory()
      .setReservationId("HansAlanTest5")
      .setStartDateTime(DateTime.now().plusMinutes(1))
      .setEndDateTime(DateTime.now().plusMinutes(25))
      .setBandwidth(100L)
      .withoutProtection()
      .create();

    reservation.getSourcePort().getVirtualPort().get().setPhysicalPort(sourcePort);
    reservation.getDestinationPort().getVirtualPort().get().setPhysicalPort(destPort);

    UpdatedReservationStatus status = subject.reserve(reservation);

    System.err.println(status);
  }

  @Test
  @Ignore
  public void shouldActivateReservation() {
    Reservation reservation = new ReservationFactory()
      .setReservationId("HansAlanTest5")
      .create();

    subject.activate(reservation);
  }

  @Test
  @Ignore
  public void shouldTerminateReservation() {
    Reservation reservation = new ReservationFactory()
      .setReservationId("HansAlanTest3")
      .create();

    subject.terminate(reservation);
  }

  private UniPort createPort(String name, String me, String ptp) {
    return new PhysicalPortFactory()
      .setNbiPort(new NbiPortFactory()
      .setNmsSapName(name)
      .setNmsPortId(me+"@"+ptp)
      .setNmsNeId(me).create())
      .create();
  }

  static {
    System.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, "true");
    System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "false");
    System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
    System.setProperty("com.sun.xml.ws.util.pipe.StandaloneTubeAssembler.dump", "true");
    System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
  }
}