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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Future;

import javax.persistence.EntityManager;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import nl.surfnet.bod.FakeTransactionOperations;
import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.domain.UniPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.RichUserDetailsFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.web.security.RichUserDetails;
import nl.surfnet.bod.web.security.Security;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.transaction.support.TransactionOperations;

@RunWith(MockitoJUnitRunner.class)
public class NocServiceTest {

  @InjectMocks
  private NocService subject;

  @Mock private ReservationService reservationServiceMock;
  @Mock private VirtualPortService virtualPortServiceMock;
  @Mock private PhysicalPortService physicalPortServiceMock;
  @Mock private EntityManager entityManagerMock;

  private TransactionOperations transactionOperations = new FakeTransactionOperations();

  private final RichUserDetails user = new RichUserDetailsFactory().addNocRole().create();

  @Before
  public void setNocUser() {
    Security.setUserDetails(user);
    subject.setTransactionOperations(transactionOperations);
  }

  @Test
  public void moveShouldRescheduleReservations() {
    DateTime start = DateTime.now().plusDays(2);
    DateTime end = DateTime.now().plusDays(5);

    UniPort oldPort = new PhysicalPortFactory().create();
    NbiPort newPort = new PhysicalPortFactory().create().getNbiPort();
    VirtualPort vPort = new VirtualPortFactory().create();
    Reservation reservation = new ReservationFactory()
      .setBandwidth(150L)
      .setName("My first reservation")
      .setStartDateTime(start)
      .setEndDateTime(end).create();

    when(virtualPortServiceMock.findAllForPhysicalPort(oldPort)).thenReturn(ImmutableList.of(vPort));
    when(reservationServiceMock.findActiveByPhysicalPort(oldPort)).thenReturn(ImmutableList.of(reservation));
    when(reservationServiceMock.cancelWithReason(any(Reservation.class), anyString(), any(RichUserDetails.class)))
      .thenReturn(Optional.<Future<Long>>of(new AsyncResult<>(reservation.getId())));

    subject.movePort(oldPort, newPort);

    ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
    verify(reservationServiceMock).create(reservationCaptor.capture());

    Reservation newReservation = reservationCaptor.getValue();
    assertThat(newReservation.getBandwidth(), is(150L));
    assertThat(newReservation.getName(), is("My first reservation"));
    assertThat(newReservation.getStartDateTime(), is(start));
    assertThat(newReservation.getEndDateTime(), is(end));
  }

  @Test
  public void oldPortShouldBeUnallocated() {
    UniPort oldPort = new PhysicalPortFactory().create();
    NbiPort newPort = new PhysicalPortFactory().create().getNbiPort();

    subject.movePort(oldPort, newPort);

    verify(physicalPortServiceMock).delete(oldPort.getId());
  }

  @Test
  public void virtualPortsShouldHaveNewPhysicalPort() {
    UniPort oldPort = new PhysicalPortFactory().create();
    NbiPort newPort = new PhysicalPortFactory().create().getNbiPort();
    VirtualPort port1 = new VirtualPortFactory().create();
    VirtualPort port2 = new VirtualPortFactory().create();

    when(virtualPortServiceMock.findAllForPhysicalPort(oldPort)).thenReturn(ImmutableList.of(port1, port2));

    subject.movePort(oldPort, newPort);

    assertThat(port1.getPhysicalPort().getNbiPort(), is(newPort));
    assertThat(port2.getPhysicalPort().getNbiPort(), is(newPort));
  }

}
