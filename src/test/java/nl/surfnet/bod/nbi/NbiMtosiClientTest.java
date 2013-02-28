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
package nl.surfnet.bod.nbi;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.xml.bind.JAXBElement;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.nbi.mtosi.InventoryRetrievalClient;
import nl.surfnet.bod.nbi.mtosi.MtosiUtils;
import nl.surfnet.bod.nbi.mtosi.ServiceComponentActivationClient;
import nl.surfnet.bod.repo.ReservationRepo;
import nl.surfnet.bod.support.ReservationFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.tmforum.mtop.fmw.xsd.nam.v1.NamingAttributeType;
import org.tmforum.mtop.msi.xsd.sir.v1.ServiceInventoryDataType.RfsList;
import org.tmforum.mtop.sb.xsd.svc.v1.ResourceFacingServiceType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceStateType;

import com.google.common.base.Optional;

@RunWith(MockitoJUnitRunner.class)
public class NbiMtosiClientTest {

  @InjectMocks
  private NbiMtosiClient subject;

  @Mock
  private ReservationRepo reservationRepo;

  @Mock
  private ServiceComponentActivationClient serviceComponentActivationClient;

  @Mock
  private InventoryRetrievalClient inventoryRetrievalClient;

  private Reservation reservation;

  @Before
  public void setUp() {
    reservation = new ReservationFactory().setReservationId(null).create();
  }

  @Test
  public void shouldGenerateUUID() {
    when(reservationRepo.saveAndFlush(reservation)).thenReturn(reservation);
    when(serviceComponentActivationClient.reserve(reservation, true)).thenReturn(reservation);

    assertThat(reservation.getReservationId(), nullValue());
    Reservation createdReservation = subject.createReservation(reservation, true);
    assertThat(createdReservation.getReservationId(), notNullValue());

    verify(serviceComponentActivationClient).reserve(createdReservation, true);
  }

  @Test
  public void findNonExistingPortByNmsIdShouldThrowUp() {

    String nmsPortId = "me@/rack=9/shelf=9/slot=9/sub_slot=9/port=9";
    try {
      subject.findPhysicalPortByNmsPortId(nmsPortId);
      fail("Exception expected");
    }
    catch (PortNotAvailableException e) {
      assertThat(e.getMessage(), containsString(nmsPortId));
    }
  }

  @Test
  public void shouldGetStatus() {
    reservation.setReservationId("123");

    RfsList rfsList = new org.tmforum.mtop.msi.xsd.sir.v1.ObjectFactory().createServiceInventoryDataTypeRfsList();

    ResourceFacingServiceType rfs = new org.tmforum.mtop.sb.xsd.svc.v1.ObjectFactory()
        .createResourceFacingServiceType();
    rfs.setServiceState(ServiceStateType.RESERVED);
    rfs.setName(MtosiUtils.createNamingAttributeType("RFS", reservation.getReservationId()));

    rfsList.getRfs().add(rfs);

    when(inventoryRetrievalClient.getRfsInventory()).thenReturn(rfsList);

    assertThat(subject.getReservationStatus(reservation.getReservationId()).get(), is(ReservationStatus.SCHEDULED));
  }

  @Test
  public void shouldGetAbsentStatusWhenReservationNotFound() {
    reservation.setReservationId("123");

    RfsList rfsList = new org.tmforum.mtop.msi.xsd.sir.v1.ObjectFactory().createServiceInventoryDataTypeRfsList();

    ResourceFacingServiceType rfs = new org.tmforum.mtop.sb.xsd.svc.v1.ObjectFactory()
        .createResourceFacingServiceType();
    rfs.setServiceState(ServiceStateType.RESERVED);
    rfs.setName(MtosiUtils.createNamingAttributeType("RFS", "no-match"));

    rfsList.getRfs().add(rfs);

    when(inventoryRetrievalClient.getRfsInventory()).thenReturn(rfsList);

    assertThat(Optional.<ReservationStatus> absent(), is(subject.getReservationStatus(reservation.getReservationId())));
  }

  @Test
  public void shouldGetAbsentStatusWhenRfsNotComplete() {
    reservation.setReservationId("123");

    RfsList rfsList = new org.tmforum.mtop.msi.xsd.sir.v1.ObjectFactory().createServiceInventoryDataTypeRfsList();

    ResourceFacingServiceType rfs = new org.tmforum.mtop.sb.xsd.svc.v1.ObjectFactory()
        .createResourceFacingServiceType();
    rfs.setServiceState(ServiceStateType.RESERVED);
    JAXBElement<NamingAttributeType> reservationIdNamingAttribute = MtosiUtils.createNamingAttributeType("RFS",
        "no-match");
    reservationIdNamingAttribute.getValue().getRdn().clear();
    rfs.setName(reservationIdNamingAttribute);

    rfsList.getRfs().add(rfs);

    when(inventoryRetrievalClient.getRfsInventory()).thenReturn(rfsList);

    assertThat(Optional.<ReservationStatus> absent(), is(subject.getReservationStatus(reservation.getReservationId())));
  }
}
