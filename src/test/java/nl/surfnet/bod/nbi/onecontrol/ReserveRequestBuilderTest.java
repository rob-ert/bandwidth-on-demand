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

import static nl.surfnet.bod.matchers.OptionalMatchers.isPresent;
import static nl.surfnet.bod.matchers.ServiceCharacteristicValueTypeMatcher.serviceCharacteristic;
import static nl.surfnet.bod.nbi.onecontrol.MtosiUtils.findRdnValue;
import static nl.surfnet.bod.nbi.onecontrol.MtosiUtils.findSscValue;
import static nl.surfnet.bod.nbi.onecontrol.ReserveRequestBuilder.TRAFFIC_MAPPING_FROM_TABLE_PRIORITY;
import static nl.surfnet.bod.nbi.onecontrol.ReserveRequestBuilder.TRAFFIC_MAPPING_TABLECOUNT;
import static nl.surfnet.bod.nbi.onecontrol.ReserveRequestBuilder.TRAFFIC_MAPPING_TO_TABLE_TRAFFICCLASS;
import static nl.surfnet.bod.util.XmlUtils.getDateTimeFromXml;
import static nl.surfnet.bod.util.XmlUtils.toGregorianCalendar;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;

import com.google.common.collect.Lists;

import nl.surfnet.bod.domain.NbiPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.ReservationEndPoint;
import nl.surfnet.bod.domain.UniPort;
import nl.surfnet.bod.support.NbiPortFactory;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.VirtualPortFactory;

import org.joda.time.DateTime;
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
    reservation.getSourcePort().getPhysicalPort().getNbiPort().setNmsSapName("sourceNmsSapName");
    reservation.getSourcePort().getPhysicalPort().getNbiPort().setNmsNeId("sourceNmsNeId");
    reservation.getSourcePort().getPhysicalPort().getNbiPort().setNmsPortId("henk@1-1-1-1");
    reservation.getDestinationPort().getPhysicalPort().getNbiPort().setNmsSapName("destinationNmsSapName");
    reservation.getDestinationPort().getPhysicalPort().getNbiPort().setNmsNeId("sourceNmsNeId");
    reservation.getDestinationPort().getPhysicalPort().getNbiPort().setNmsPortId("joop@1-1-1-4");

    ReserveRequest reserveRequest = ReserveRequestBuilder.createReservationRequest(reservation);

    assertThat(reserveRequest.getExpiringTime(), is(toGregorianCalendar(reservation.getEndDateTime().get())));

    ResourceFacingServiceType rfs = reserveRequest.getRfsCreateData();

    assertThat("name", rfs.getName().getValue().getRdn().get(0).getValue(), is("123"));
    assertTrue("mandatory", rfs.isIsMandatory());
    assertTrue("stateful", rfs.isIsStateful());
    assertThat("adminState", rfs.getAdminState(), is(AdminStateType.UNLOCKED));
    assertThat("serviceState", rfs.getServiceState(), is(ServiceStateType.RESERVED));
    assertThat(rfs.getDescribedByList(), hasSize(4));

    assertThat("protectionLevel", findSscValue("ProtectionLevel", rfs.getDescribedByList()), isPresent("PartiallyProtected"));
    assertThat("serviceType", findSscValue("ServiceType", rfs.getDescribedByList()), isPresent("EPL"));

    String startDateTime = MtosiUtils.findSscValue("StartTime", rfs.getDescribedByList()).get();
    assertThat(getDateTimeFromXml(startDateTime), is(reservation.getStartDateTime()));

    assertThat(rfs.getSapList(), hasSize(2));
    ServiceAccessPointType sourceSapList = rfs.getSapList().get(0);

    assertThat(sourceSapList.getDescribedByList().get(0), serviceCharacteristic("TrafficMappingTableCount", TRAFFIC_MAPPING_TABLECOUNT));
    assertThat(sourceSapList.getDescribedByList().get(1), serviceCharacteristic("TrafficMappingFrom_Table_Priority", TRAFFIC_MAPPING_FROM_TABLE_PRIORITY));

    String tmttt = MtosiUtils.findSscValue("TrafficMappingTo_Table_TrafficClass", sourceSapList.getDescribedByList()).get();
    assertThat(tmttt, is(TRAFFIC_MAPPING_TO_TABLE_TRAFFICCLASS));

    List<ServiceCharacteristicValueType> sourceSSCList = sourceSapList.getDescribedByList();
    assertThat(sourceSSCList, hasSize(6));
    assertThat(sourceSSCList, hasItem(serviceCharacteristic("TrafficMappingTableCount", ReserveRequestBuilder.TRAFFIC_MAPPING_TABLECOUNT)));
    assertThat(sourceSSCList, hasItem(serviceCharacteristic("TrafficMappingFrom_Table_Priority", ReserveRequestBuilder.TRAFFIC_MAPPING_FROM_TABLE_PRIORITY)));
  }

  @Test
  public void admission_control_should_be_set_to_loose_when_bandwidth_below_or_equal_thousand() {
    for (Long bandwidth : Lists.newArrayList(1L, 10L, 500L, 1000L)) {
      Reservation reservation = new ReservationFactory().setBandwidth(bandwidth).create();
      ResourceFacingServiceType rfs = ReserveRequestBuilder.createBasicRfsData(reservation);

      assertThat(rfs.getDescribedByList(), hasItem(serviceCharacteristic("AdmissionControl", "Loose")));
    }
  }

  @Test
  public void admission_control_should_be_set_to_strict_when_bandwidth_above_thousand() {
    for (Long bandwidth : Lists.newArrayList(1001L, 5000L, 10000L)) {
      Reservation reservation = new ReservationFactory().setBandwidth(bandwidth).create();
      ResourceFacingServiceType rfs = ReserveRequestBuilder.createBasicRfsData(reservation);

      assertThat(rfs.getDescribedByList(), hasItem(serviceCharacteristic("AdmissionControl", "Strict")));
    }
  }

  @Test
  public void should_add_dynamic_characteristics_with_vlan_present() {
    UniPort physicalPort = new PhysicalPortFactory().setNbiPort(new NbiPortFactory().setNmsPortId("test@1-1-1-2").setVlanRequired(true).create()).create();
    Reservation reservation = new ReservationFactory().withProtection().setBandwidth(1024L).create();
    ReservationEndPoint port = new ReservationEndPoint(new VirtualPortFactory().setVlanId(3).setPhysicalPort(physicalPort).create());

    ServiceAccessPointType sap = ReserveRequestBuilder.getSap(reservation, port);

    assertThat(findSscValue("TrafficMappingFrom_Table_VID", sap.getDescribedByList()), isPresent("3"));
    assertThat(findSscValue("InterfaceType", sap.getDescribedByList()), isPresent("UNI"));
    assertThat(findSscValue("TrafficMappingTo_Table_IngressCIR", sap.getDescribedByList()), isPresent("1024"));
  }

  @Test
  public void should_add_dynamic_characteristics_with_vlan_absent() {
    UniPort physicalPort = new PhysicalPortFactory().setNbiPort(new NbiPortFactory().setNmsPortId("test@1-1-1-2").setVlanRequired(false).create()).create();
    Reservation reservation = new ReservationFactory().withoutProtection().setBandwidth(1024L).create();
    ReservationEndPoint port = new ReservationEndPoint(new VirtualPortFactory().setVlanId(null).setPhysicalPort(physicalPort).create());

    ServiceAccessPointType sap = ReserveRequestBuilder.getSap(reservation, port);

    assertThat(sap.getDescribedByList(), hasItem(serviceCharacteristic("TrafficMappingFrom_Table_VID", "all")));
    assertThat(sap.getDescribedByList(), hasItem(serviceCharacteristic("InterfaceType", "UNI")));
    assertThat(sap.getDescribedByList(), hasItem(serviceCharacteristic("TrafficMappingTo_Table_IngressCIR", "1024")));
  }

  @Test
  public void should_add_static_characteristics() {
    UniPort physicalPort = new PhysicalPortFactory().setNbiPort(
        new NbiPortFactory().setNmsSapName("SAP-TEST").setNmsNeId("NeId")
            .setNmsPortId(MtosiUtils.composeNmsPortId("Me", "1-1-1-1")).create()).create();
    ReservationEndPoint port = new ReservationEndPoint(new VirtualPortFactory().setPhysicalPort(physicalPort).create());
    Reservation reservation = new ReservationFactory().setReservationId("ReservationId").create();

    ServiceAccessPointType sap = ReserveRequestBuilder.getSap(reservation, port);

    assertThat(findRdnValue("MD", sap.getResourceRef()), isPresent(ReserveRequestBuilder.MANAGING_DOMAIN));
    assertThat(findRdnValue("ME", sap.getResourceRef()), isPresent("NeId"));
    assertThat(findRdnValue("PTP", sap.getResourceRef()), isPresent(MtosiUtils.convertToLongPtP("1-1-1-1")));
    assertThat(findRdnValue("CTP", sap.getResourceRef()), isPresent("/eth=ReservationId"));
  }

  @Test
  public void shoud_create_service_access_point() {
    NbiPort port = new NbiPortFactory().setNmsSapName("SAP-TEST").setNmsNeId("NeId")
            .setNmsPortId(MtosiUtils.composeNmsPortId("Me", "1-1-1-1")).create();

    ServiceAccessPointType sap = ReserveRequestBuilder.createServiceAccessPoint(port, "ReservationId");

    assertThat(findRdnValue("MD", sap.getResourceRef()), isPresent("CIENA/OneControl"));
    assertThat(findRdnValue("ME", sap.getResourceRef()), isPresent("NeId"));
    assertThat(findRdnValue("PTP", sap.getResourceRef()), isPresent("/rack=1/shelf=1/slot=1/port=1"));
    assertThat(findRdnValue("CTP", sap.getResourceRef()), isPresent("/eth=ReservationId"));
  }

  @Test
  public void should_create_reserve_request_without_end_time() throws DatatypeConfigurationException {
    Reservation reservation = new ReservationFactory().setEndDateTime(null).create();
    reservation.getSourcePort().getPhysicalPort().getNbiPort().setNmsPortId("henk@1-1-1-1");
    reservation.getDestinationPort().getPhysicalPort().getNbiPort().setNmsPortId("joop@1-1-1-4");

    ReserveRequest reserveRequest = ReserveRequestBuilder.createReservationRequest(reservation);

    assertThat(reserveRequest.getExpiringTime().getYear(), is(2029));
  }

  @Test
  public void should_create_reserve_request_with_end_time() {
    Reservation reservation = new ReservationFactory().setEndDateTime(new DateTime(2014, 1, 22, 12, 0)).create();
    reservation.getSourcePort().getPhysicalPort().getNbiPort().setNmsPortId("henk@1-1-1-1");
    reservation.getDestinationPort().getPhysicalPort().getNbiPort().setNmsPortId("joop@1-1-1-4");

    ReserveRequest reserveRequest = ReserveRequestBuilder.createReservationRequest(reservation);

    assertThat(reserveRequest.getExpiringTime().getYear(), is(2014));
    assertThat(reserveRequest.getExpiringTime().getMonth(), is(1));
    assertThat(reserveRequest.getExpiringTime().getDay(), is(22));
  }
}
