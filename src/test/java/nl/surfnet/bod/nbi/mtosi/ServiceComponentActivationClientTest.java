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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.support.ReservationFactory;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.tmforum.mtop.sa.wsdl.scai.v1_0.ReserveException;

@RunWith(MockitoJUnitRunner.class)
public class ServiceComponentActivationClientTest {

  @InjectMocks
  private ServiceComponentActivationClient subject;

  @Test
  public void shouldHandleInitialReservationException() {
    Reservation reservation = getTestReservation();
    subject.handleInitialReservationException(reservation, new ReserveException("SAP is in use", null));

    assertThat(reservation.getFailedReason(), is("SAP is in use"));
    assertThat(reservation.getStatus(), is(ReservationStatus.NOT_ACCEPTED));
  }

  private Reservation getTestReservation() {
    Reservation reservation = new ReservationFactory().setStartDateTime(DateTime.now().plusYears(2))
        .setEndDateTime(DateTime.now().plusYears(2).plusDays(3)).setName("mtosiSurfTest2").create();

    reservation.getSourcePort().getPhysicalPort().setNmsSapName("SAP-00:03:18:58:cf:b0-50");
    reservation.getSourcePort().getPhysicalPort().setNmsPortId("1-1-1-8");
    reservation.getSourcePort().getPhysicalPort().setNmsNeId("00:03:18:58:cf:b0");

    reservation.getDestinationPort().getPhysicalPort().setNmsSapName("SAP-00:03:18:58:ce:20-50");
    reservation.getDestinationPort().getPhysicalPort().setNmsPortId("1-1-1-1");
    reservation.getDestinationPort().getPhysicalPort().setNmsNeId("00:03:18:58:ce:20");
    return reservation;
  }

}
