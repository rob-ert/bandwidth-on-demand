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

import javax.xml.bind.Marshaller;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.util.TestHelper;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;


public class ServiceComponentActivationClientTestIntegration {

  static {
    System.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, "true");
//    System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "false");
//    System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
//    System.setProperty("com.sun.xml.ws.util.pipe.StandaloneTubeAssembler.dump", "true");
//    System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
  }

  private ServiceComponentActivationClient subject;

  @Before
  public void setup() {
    //String endPoint = productionProperties().getProperty("nbi.mtosi.service.reserve.endpoint");
    String endPoint = TestHelper.devProperties().getProperty("nbi.mtosi.service.reserve.endpoint");
    subject = new ServiceComponentActivationClient(endPoint);
  }

  @Test
//  @Ignore("Needs access to london server... is more like integration, but now only for testing..")
  public void reserve() {

    Reservation reservation = new ReservationFactory()
      .setStartDateTime(DateTime.now().plusYears(2))
      .setEndDateTime(DateTime.now().plusYears(2).plusDays(3))
      .setName("mtosiSurfTest2").create();

    reservation.getSourcePort().getPhysicalPort().setNmsSapName("SAP-00:03:18:58:cf:b0-50");
    reservation.getSourcePort().getPhysicalPort().setNmsPortId("1-1-1-8");
    reservation.getSourcePort().getPhysicalPort().setNmsNeId("00:03:18:58:cf:b0");

    reservation.getDestinationPort().getPhysicalPort().setNmsSapName("SAP-00:03:18:58:ce:20-50");
    reservation.getDestinationPort().getPhysicalPort().setNmsPortId("1-1-1-1");
    reservation.getDestinationPort().getPhysicalPort().setNmsNeId("00:03:18:58:ce:20");

    subject.reserve(reservation, false);
  }

}