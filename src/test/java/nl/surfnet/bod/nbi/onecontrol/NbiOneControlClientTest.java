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

import static nl.surfnet.bod.matchers.OptionalMatchers.isAbsent;
import static nl.surfnet.bod.nbi.onecontrol.MtosiUtils.createComonObjectInfoTypeName;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;

import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationStatus;
import nl.surfnet.bod.nbi.PortNotAvailableException;
import nl.surfnet.bod.nbi.onecontrol.InventoryRetrievalClient;
import nl.surfnet.bod.nbi.onecontrol.NbiOneControlClient;
import nl.surfnet.bod.nbi.onecontrol.ServiceComponentActivationClient;
import nl.surfnet.bod.repo.ReservationRepo;
import nl.surfnet.bod.support.ReservationFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.tmforum.mtop.msi.xsd.sir.v1.ServiceInventoryDataType.RfsList;
import org.tmforum.mtop.sb.xsd.svc.v1.ResourceFacingServiceType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceStateType;

@RunWith(MockitoJUnitRunner.class)
public class NbiOneControlClientTest {

  @InjectMocks
  private NbiOneControlClient subject;

  @Mock private ReservationRepo reservationRepo;
  @Mock private ServiceComponentActivationClient serviceComponentActivationClient;
  @Mock private InventoryRetrievalClient inventoryRetrievalClient;

  private Reservation reservation;

  @Before
  public void setUp() {
    reservation = new ReservationFactory().setReservationId(null).create();
  }

  @Test
  public void should_throw_port_not_available_exception_for_invalid_nms_port_id() {
    String nmsPortId = "me@/rack=9/shelf=9/slot=9/sub_slot=9/port=9";
    try {
      subject.findPhysicalPortByNmsPortId(nmsPortId);
      fail("PortNotAvailableException expected");
    } catch (PortNotAvailableException e) {
      assertThat(e.getMessage(), containsString(nmsPortId));
    }
  }

  @Test
  public void should_get_status() {
    reservation.setReservationId("123");


    ResourceFacingServiceType rfs = new org.tmforum.mtop.sb.xsd.svc.v1.ObjectFactory().createResourceFacingServiceType()
      .withServiceState(ServiceStateType.RESERVED)
      .withName(createComonObjectInfoTypeName("RFS", reservation.getReservationId()));

    RfsList rfsList = new org.tmforum.mtop.msi.xsd.sir.v1.ObjectFactory().createServiceInventoryDataTypeRfsList()
      .withRfs(rfs);

    when(inventoryRetrievalClient.getRfsInventory()).thenReturn(Optional.of(rfsList));

    ReservationStatus status = subject.getReservationStatus(reservation.getReservationId()).get();

    assertThat(status, is(ReservationStatus.SCHEDULED));
  }

  @Test
  public void should_get_absent_status_when_reservation_not_found() {
    reservation.setReservationId("123");

    ResourceFacingServiceType rfs = new org.tmforum.mtop.sb.xsd.svc.v1.ObjectFactory().createResourceFacingServiceType()
      .withServiceState(ServiceStateType.RESERVED)
      .withName(createComonObjectInfoTypeName("RFS", "no-match"));

    RfsList rfsList = new org.tmforum.mtop.msi.xsd.sir.v1.ObjectFactory().createServiceInventoryDataTypeRfsList()
        .withRfs(rfs);

    when(inventoryRetrievalClient.getRfsInventory()).thenReturn(Optional.of(rfsList));

    Optional<ReservationStatus> status = subject.getReservationStatus(reservation.getReservationId());

    assertThat(status, isAbsent());
  }

}