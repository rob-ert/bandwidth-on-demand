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

import static nl.surfnet.bod.domain.ProtectionType.PROTECTED;
import static nl.surfnet.bod.domain.ProtectionType.UNPROTECTED;
import static nl.surfnet.bod.matchers.RdnValueTypeMatcher.rdnValue;
import static nl.surfnet.bod.matchers.ServiceCharacteristicValueTypeMatcher.serviceCharacteristic;
import static nl.surfnet.bod.nbi.mtosi.ReserveRequestBuilder.TRAFFIC_MAPPING_FROM_TABLE_PRIORITY;
import static nl.surfnet.bod.nbi.mtosi.ReserveRequestBuilder.TRAFFIC_MAPPING_TABLECOUNT;
import static nl.surfnet.bod.nbi.mtosi.ReserveRequestBuilder.TRAFFIC_MAPPING_TO_TABLE_TRAFFICCLASS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringWriter;
import java.net.URL;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.domain.VirtualPort;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.util.XmlUtils;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.tmforum.mtop.sa.xsd.scai.v1.ReserveRequest;
import org.tmforum.mtop.sb.xsd.svc.v1.*;
import org.xml.sax.SAXException;

import com.ciena.mtop.tmw.xsd.coi.v1.Nvs;

@RunWith(MockitoJUnitRunner.class)
public class ReserveRequestBuilderTest {
  @InjectMocks
  private final ReserveRequestBuilder subject = new ReserveRequestBuilder();

  private boolean schemaValidation;

  @Test
  public void shouldMarshall() throws JAXBException {
    Reservation reservation = new ReservationFactory().setReservationId("123").create();
    reservation.getSourcePort().getPhysicalPort().setNmsSapName("sourceNmsSapName");
    reservation.getSourcePort().getPhysicalPort().setNmsNeId("sourceNmsNeId");
    reservation.getSourcePort().getPhysicalPort().setNmsPortId("1-1-1-1");

    reservation.getDestinationPort().getPhysicalPort().setNmsSapName("destinationNmsSapName");
    reservation.getDestinationPort().getPhysicalPort().setNmsNeId("sourceNmsNeId");
    reservation.getDestinationPort().getPhysicalPort().setNmsPortId("1-1-1-4");

    ReserveRequest reserveRequest = subject.createReservationRequest(reservation, false, Long.MIN_VALUE);

    assertThat(reserveRequest.getExpiringTime(),
        Matchers.is(XmlUtils.toGregorianCalendar(reservation.getEndDateTime())));

    ResourceFacingServiceType rfs = reserveRequest.getRfsCreateData();
    assertRfs(rfs);

    assertThat(rfs.getDescribedByList(), hasSize(2));

    String startDateTime = MtosiUtils.findSscValue("startTime", rfs.getDescribedByList()).get();
    assertThat(XmlUtils.getDateTimeFromXml(startDateTime), is(reservation.getStartDateTime()));

    assertThat(rfs.getDescribedByList(), hasItem(serviceCharacteristic("AdmissionControl", "Strict")));

    assertThat(rfs.getSapList(), hasSize(2));
    ServiceAccessPointType sourceSapList = rfs.getSapList().get(0);
    assertSourceSapList(sourceSapList);

    List<ServiceCharacteristicValueType> sourceSSCList = sourceSapList.getDescribedByList();
    assertThat(sourceSSCList, hasSize(8));

    assertThat(sourceSSCList,
        hasItem(serviceCharacteristic("TrafficMappingTableCount", ReserveRequestBuilder.TRAFFIC_MAPPING_TABLECOUNT)));
    assertThat(
        sourceSSCList,
        hasItem(serviceCharacteristic("TrafficMappingFrom_Table_Priority",
            ReserveRequestBuilder.TRAFFIC_MAPPING_FROM_TABLE_PRIORITY)));

    JAXBContext context = JAXBContext.newInstance(ReserveRequest.class, Nvs.class);

    Marshaller marshaller = context.createMarshaller();
    // Enable schema validation

    if (schemaValidation) {
      marshaller.setSchema(getMtosiSchema());
    }
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

    // Create a stringWriter to hold the XML
    StringWriter stringWriter = new StringWriter();
    marshaller.marshal(reserveRequest, stringWriter);
  }

  private void assertSourceSapList(ServiceAccessPointType sourceSapList) {
    assertThat(sourceSapList.getDescribedByList().get(0),
        serviceCharacteristic("TrafficMappingTableCount", TRAFFIC_MAPPING_TABLECOUNT));

    assertThat(sourceSapList.getDescribedByList().get(1),
        serviceCharacteristic("TrafficMappingFrom_Table_Priority", TRAFFIC_MAPPING_FROM_TABLE_PRIORITY));

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

  @Test
  public void shouldAddDynamicCharacteristicsWithVlanPresent() {
    PhysicalPort physicalPort = new PhysicalPortFactory()
      .setVlanRequired(true).create();
    Reservation reservation = new ReservationFactory()
      .withProtection()
      .setBandwidth(1024)
      .create();
    VirtualPort port = new VirtualPortFactory()
      .setVlanId(3)
      .setPhysicalPort(physicalPort).create();

    ServiceAccessPointType sap = subject.getSap(reservation, port, 2L);

    assertThat(sap.getDescribedByList(), hasItem(serviceCharacteristic("ServiceType", "EVPL")));
    assertThat(sap.getDescribedByList(), hasItem(serviceCharacteristic("TrafficMappingFrom_Table_VID", "3")));
    assertThat(sap.getDescribedByList(), hasItem(serviceCharacteristic("InterfaceType", "UNI-N")));
    assertThat(sap.getDescribedByList(), hasItem(serviceCharacteristic("TrafficMappingTo_Table_IngressCIR", "1024")));
    assertThat(sap.getDescribedByList(), hasItem(serviceCharacteristic("ProtectionLevel", PROTECTED.getMtosiName())));
  }

  @Test
  public void shouldAddDynamicCharacteristicsWithVlanAbsent() {
    PhysicalPort physicalPort = new PhysicalPortFactory()
      .setVlanRequired(false).create();
    Reservation reservation = new ReservationFactory()
      .withoutProtection()
      .setBandwidth(1024)
      .create();
    VirtualPort port = new VirtualPortFactory()
      .setVlanId(null)
      .setPhysicalPort(physicalPort).create();

    ServiceAccessPointType sap = subject.getSap(reservation, port, 5L);

    assertThat(sap.getDescribedByList(), hasItem(serviceCharacteristic("ServiceType", "EPL")));
    assertThat(sap.getDescribedByList(), hasItem(serviceCharacteristic("TrafficMappingFrom_Table_VID", "all")));
    assertThat(sap.getDescribedByList(), hasItem(serviceCharacteristic("InterfaceType", "UNI-N")));
    assertThat(sap.getDescribedByList(), hasItem(serviceCharacteristic("TrafficMappingTo_Table_IngressCIR", "1024")));
    assertThat(sap.getDescribedByList(), hasItem(serviceCharacteristic("ProtectionLevel", UNPROTECTED.getMtosiName())));
  }

  @Test
  public void shouldAddStaticCharacteristics() {
    PhysicalPort physicalPort = new PhysicalPortFactory()
      .setNmsSapName("SAP-TEST").setNmsNeId("NeId")
      .setNmsPortId(MtosiUtils.composeNmsPortId("Me", "1-1-1-1")).create();
    Reservation reservation = new ReservationFactory().create();
    VirtualPort port = new VirtualPortFactory().setPhysicalPort(physicalPort).create();

    ServiceAccessPointType sap = subject.getSap(reservation, port, 1L);

    assertThat(sap.getResourceRef().getRdn(), hasItem(rdnValue("MD", ReserveRequestBuilder.MANAGING_DOMAIN)));
    assertThat(sap.getResourceRef().getRdn(), hasItem(rdnValue("ME", "NeId")));
    assertThat(sap.getResourceRef().getRdn(), hasItem(rdnValue("PTP", MtosiUtils.convertToLongPtP("1-1-1-1"))));
  }

  @Test
  public void shoudCreateServiceAccessPoint() {
    PhysicalPort port = new PhysicalPortFactory()
      .setNmsSapName("SAP-TEST").setNmsNeId("NeId")
      .setNmsPortId(MtosiUtils.composeNmsPortId("Me", "1-1-1-1")).create();

    ServiceAccessPointType serviceAccessPoint = subject.createServiceAccessPoint(port, 123);

    assertThat(serviceAccessPoint.getResourceRef().getRdn(), hasItem(rdnValue("MD", "CIENA/OneControl")));
    assertThat(serviceAccessPoint.getResourceRef().getRdn(), hasItem(rdnValue("ME", "NeId")));
    assertThat(serviceAccessPoint.getResourceRef().getRdn(), hasItem(rdnValue("PTP", "/rack=1/shelf=1/slot=1/port=1")));

  }

  private javax.xml.validation.Schema getMtosiSchema() {
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    URL xsdUrl = getClass().getClassLoader().getResource(
        "mtosi/2.1/DDPs/ServiceActivation/IIS/xsd/ServiceComponentActivationInterfaceMessages.xsd");
    assertNotNull(xsdUrl);

    Schema schema = null;
    try {
      schema = schemaFactory.newSchema(xsdUrl);
    }
    catch (SAXException e) {
      fail("Error locating schema");
    }

    return schema;
  }
}
