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

import static nl.surfnet.bod.domain.ProtectionType.PROTECTED;
import static nl.surfnet.bod.domain.ProtectionType.UNPROTECTED;
import static nl.surfnet.bod.matchers.OptionalMatchers.isPresent;
import static nl.surfnet.bod.matchers.ServiceCharacteristicValueTypeMatcher.serviceCharacteristic;
import static nl.surfnet.bod.nbi.mtosi.MtosiUtils.findRdnValue;
import static nl.surfnet.bod.nbi.mtosi.MtosiUtils.findSscValue;
import static nl.surfnet.bod.nbi.mtosi.ReserveRequestBuilder.TRAFFIC_MAPPING_FROM_TABLE_PRIORITY;
import static nl.surfnet.bod.nbi.mtosi.ReserveRequestBuilder.TRAFFIC_MAPPING_TABLECOUNT;
import static nl.surfnet.bod.nbi.mtosi.ReserveRequestBuilder.TRAFFIC_MAPPING_TO_TABLE_TRAFFICCLASS;
import static nl.surfnet.bod.util.XmlUtils.getDateTimeFromXml;
import static nl.surfnet.bod.util.XmlUtils.toGregorianCalendar;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.VirtualPortFactory;

import org.junit.Test;
import org.tmforum.mtop.sa.xsd.scai.v1.ReserveRequest;
import org.tmforum.mtop.sb.xsd.svc.v1.AdminStateType;
import org.tmforum.mtop.sb.xsd.svc.v1.ResourceFacingServiceType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceAccessPointType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceCharacteristicValueType;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceStateType;

public class ReserveRequestBuilderTest {

  @Test
  public void should_create_a_full_reserve_request() throws Exception {
    Reservation reservation = new ReservationFactory().setReservationId("123").create();
    reservation.getSourcePort().getPhysicalPort().setNmsSapName("sourceNmsSapName");
    reservation.getSourcePort().getPhysicalPort().setNmsNeId("sourceNmsNeId");
    reservation.getSourcePort().getPhysicalPort().setNmsPortId("1-1-1-1");
    reservation.getDestinationPort().getPhysicalPort().setNmsSapName("destinationNmsSapName");
    reservation.getDestinationPort().getPhysicalPort().setNmsNeId("sourceNmsNeId");
    reservation.getDestinationPort().getPhysicalPort().setNmsPortId("1-1-1-4");

    ReserveRequest reserveRequest = ReserveRequestBuilder.createReservationRequest(reservation, false);

    assertThat(reserveRequest.getExpiringTime(), is(toGregorianCalendar(reservation.getEndDateTime())));

    ResourceFacingServiceType rfs = reserveRequest.getRfsCreateData();

    assertRfs(rfs);
    assertThat(rfs.getDescribedByList(), hasSize(2));

    String startDateTime = MtosiUtils.findSscValue("StartTime", rfs.getDescribedByList()).get();

    assertThat(getDateTimeFromXml(startDateTime), is(reservation.getStartDateTime()));
    assertThat(rfs.getDescribedByList(), hasItem(serviceCharacteristic("AdmissionControl", "Strict")));
    assertThat(rfs.getSapList(), hasSize(2));

    ServiceAccessPointType sourceSapList = rfs.getSapList().get(0);

    assertSourceSapList(sourceSapList);

    List<ServiceCharacteristicValueType> sourceSSCList = sourceSapList.getDescribedByList();

    assertThat(sourceSSCList, hasSize(8));
    assertThat(sourceSSCList, hasItem(serviceCharacteristic("TrafficMappingTableCount", ReserveRequestBuilder.TRAFFIC_MAPPING_TABLECOUNT)));
    assertThat(sourceSSCList, hasItem(serviceCharacteristic("TrafficMappingFrom_Table_Priority", ReserveRequestBuilder.TRAFFIC_MAPPING_FROM_TABLE_PRIORITY)));

  }

  @Test
  public void should_add_dynamic_characteristics_with_vlan_present() {
    PhysicalPort physicalPort = new PhysicalPortFactory().setVlanRequired(true).create();
    Reservation reservation = new ReservationFactory().withProtection().setBandwidth(1024).create();
    VirtualPort port = new VirtualPortFactory().setVlanId(3).setPhysicalPort(physicalPort).create();

    ServiceAccessPointType sap = ReserveRequestBuilder.getSap(reservation, port);

    assertThat(findSscValue("ServiceType", sap.getDescribedByList()), isPresent("EVPL"));
    assertThat(findSscValue("TrafficMappingFrom_Table_VID", sap.getDescribedByList()), isPresent("3"));
    assertThat(findSscValue("InterfaceType", sap.getDescribedByList()), isPresent("UNI-N"));
    assertThat(findSscValue("TrafficMappingTo_Table_IngressCIR", sap.getDescribedByList()), isPresent("1024"));
    assertThat(findSscValue("ProtectionLevel", sap.getDescribedByList()), isPresent(PROTECTED.getMtosiName()));
  }

  @Test
  public void should_add_dynamic_characteristics_with_vlan_absent() {
    PhysicalPort physicalPort = new PhysicalPortFactory().setVlanRequired(false).create();
    Reservation reservation = new ReservationFactory().withoutProtection().setBandwidth(1024).create();
    VirtualPort port = new VirtualPortFactory().setVlanId(null).setPhysicalPort(physicalPort).create();

    ServiceAccessPointType sap = ReserveRequestBuilder.getSap(reservation, port);

    assertThat(sap.getDescribedByList(), hasItem(serviceCharacteristic("ServiceType", "EPL")));
    assertThat(sap.getDescribedByList(), hasItem(serviceCharacteristic("TrafficMappingFrom_Table_VID", "all")));
    assertThat(sap.getDescribedByList(), hasItem(serviceCharacteristic("InterfaceType", "UNI-N")));
    assertThat(sap.getDescribedByList(), hasItem(serviceCharacteristic("TrafficMappingTo_Table_IngressCIR", "1024")));
    assertThat(sap.getDescribedByList(), hasItem(serviceCharacteristic("ProtectionLevel", UNPROTECTED.getMtosiName())));
  }

  @Test
  public void should_add_static_characteristics() {
    PhysicalPort physicalPort = new PhysicalPortFactory()
      .setNmsSapName("SAP-TEST").setNmsNeId("NeId")
      .setNmsPortId(MtosiUtils.composeNmsPortId("Me", "1-1-1-1")).create();
    VirtualPort port = new VirtualPortFactory().setPhysicalPort(physicalPort).create();
    Reservation reservation = new ReservationFactory().setReservationId("ReservationId").create();

    ServiceAccessPointType sap = ReserveRequestBuilder.getSap(reservation, port);

    assertThat(findRdnValue("MD", sap.getResourceRef()), isPresent(ReserveRequestBuilder.MANAGING_DOMAIN));
    assertThat(findRdnValue("ME", sap.getResourceRef()), isPresent("NeId"));
    assertThat(findRdnValue("PTP", sap.getResourceRef()), isPresent(MtosiUtils.convertToLongPtP("1-1-1-1")));
    assertThat(findRdnValue("CTP", sap.getResourceRef()), isPresent("/eth=ReservationId"));
  }

  @Test
  public void shoud_create_service_access_point() {
    PhysicalPort port = new PhysicalPortFactory()
      .setNmsSapName("SAP-TEST").setNmsNeId("NeId")
      .setNmsPortId(MtosiUtils.composeNmsPortId("Me", "1-1-1-1")).create();

    ServiceAccessPointType sap = ReserveRequestBuilder.createServiceAccessPoint(port, "ReservationId");

    assertThat(findRdnValue("MD", sap.getResourceRef()), isPresent("CIENA/OneControl"));
    assertThat(findRdnValue("ME", sap.getResourceRef()), isPresent("NeId"));
    assertThat(findRdnValue("PTP", sap.getResourceRef()), isPresent("/rack=1/shelf=1/slot=1/port=1"));
    assertThat(findRdnValue("CTP", sap.getResourceRef()), isPresent("/eth=ReservationId"));
  }

  private void assertSourceSapList(ServiceAccessPointType sourceSapList) {
    assertThat(sourceSapList.getDescribedByList().get(0), serviceCharacteristic("TrafficMappingTableCount", TRAFFIC_MAPPING_TABLECOUNT));
    assertThat(sourceSapList.getDescribedByList().get(1), serviceCharacteristic("TrafficMappingFrom_Table_Priority", TRAFFIC_MAPPING_FROM_TABLE_PRIORITY));

    String tmttt = MtosiUtils.findSscValue("TrafficMappingTo_Table_TrafficClass", sourceSapList.getDescribedByList()).get();

    assertThat(tmttt, is(TRAFFIC_MAPPING_TO_TABLE_TRAFFICCLASS));
  }

  private void assertRfs(ResourceFacingServiceType rfs) {
    assertThat(rfs.getName().getValue().getRdn().get(0).getValue(), is("123"));
    assertTrue(rfs.isIsMandatory());
    assertTrue(rfs.isIsStateful());
    assertThat(rfs.getAdminState(), is(AdminStateType.UNLOCKED));
    assertThat(rfs.getServiceState(), is(ServiceStateType.RESERVED));
  }

}
