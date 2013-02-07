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

import static nl.surfnet.bod.matchers.ServiceCharacteristicValueTypeMatcher.hasServiceCharacteristic;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import nl.surfnet.bod.domain.ProtectionType;
import nl.surfnet.bod.domain.Reservation;
import nl.surfnet.bod.support.ReservationFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.tmforum.mtop.sa.xsd.scai.v1.ReserveRequest;
import org.tmforum.mtop.sb.xsd.svc.v1.ServiceCharacteristicValueType;
import org.xml.sax.SAXException;

import com.ciena.mtop.tmw.xsd.coi.v1.Nvs;
import com.google.common.base.Optional;

@RunWith(MockitoJUnitRunner.class)
public class ReserveRequestBuilderTest {
  @InjectMocks
  private final ReserveRequestBuilder subject = new ReserveRequestBuilder();

  private boolean schemaValidation;

  @Test
  public void shouldMarshall() throws JAXBException {
    Reservation reservation = new ReservationFactory().create();
    reservation.getSourcePort().getPhysicalPort().setNmsSapName("sourceNmsSapName");
    reservation.getSourcePort().getPhysicalPort().setNmsNeId("sourceNmsNeId");
    reservation.getSourcePort().getPhysicalPort().setNmsPortId("1-1-1-1");

    reservation.getDestinationPort().getPhysicalPort().setNmsSapName("destinationNmsSapName");
    reservation.getDestinationPort().getPhysicalPort().setNmsNeId("sourceNmsNeId");
    reservation.getDestinationPort().getPhysicalPort().setNmsPortId("1-1-1-4");

    ReserveRequest reserveRequest = subject.createReservationRequest(reservation, false);

    final JAXBContext context = JAXBContext.newInstance(ReserveRequest.class, Nvs.class);

    final Marshaller marshaller = context.createMarshaller();
    // Enable schema validation

    if (schemaValidation) {
      marshaller.setSchema(getMtosiSchema());
    }
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

    // Create a stringWriter to hold the XML
    final StringWriter stringWriter = new StringWriter();
    marshaller.marshal(reserveRequest, stringWriter);
  }

  @Test
  public void shouldAddDynamicCharacteristicsWithVlanPresent() {
    List<ServiceCharacteristicValueType> describedByList = new ArrayList<>();

    subject.addDynamicCharacteristicsTo(Optional.<Integer> of(3), ProtectionType.PROTECTED, 1024, "UNI-N",
        describedByList);

    assertThat(describedByList, hasSize(5));

    assertThat(describedByList, hasItem(hasServiceCharacteristic("TrafficMappingFrom_Table_VID", "3")));
    assertThat(describedByList, hasItem(hasServiceCharacteristic("ProtectionLevel", ProtectionType.PROTECTED
        .getMtosiName())));
    assertThat(describedByList, hasItem(hasServiceCharacteristic("TrafficMappingTo_Table_IngressCIR", "1024")));
    assertThat(describedByList, hasItem(hasServiceCharacteristic("ServiceType", "EVPL")));
    assertThat(describedByList, hasItem(hasServiceCharacteristic("InterfaceType", "UNI-N")));
  }

  @Test
  public void shouldAddDynamicCharacteristicsWithVlanAbsent() {
    List<ServiceCharacteristicValueType> describedByList = new ArrayList<>();

    subject.addDynamicCharacteristicsTo(Optional.<Integer> absent(), ProtectionType.UNPROTECTED, 1024, "UNI-N",
        describedByList);

    assertThat(describedByList, hasSize(5));

    assertThat(describedByList, hasItem(hasServiceCharacteristic("TrafficMappingFrom_Table_VID", "all")));
    assertThat(describedByList, hasItem(hasServiceCharacteristic("ProtectionLevel", ProtectionType.UNPROTECTED
        .getMtosiName())));
    assertThat(describedByList, hasItem(hasServiceCharacteristic("TrafficMappingTo_Table_IngressCIR", "1024")));
    assertThat(describedByList, hasItem(hasServiceCharacteristic("ServiceType", "EPL")));
    assertThat(describedByList, hasItem(hasServiceCharacteristic("InterfaceType", "UNI-N")));
  }

  @Test
  public void shouldMapProtectionTypeProtected() {
    assertThat(ProtectionType.PROTECTED.getMtosiName(), is("Partially Protected"));
  }

  @Test
  public void shouldMapProtectionTypeUnprotected() {
    assertThat(ProtectionType.UNPROTECTED.getMtosiName(), is("Unprotected"));
  }

  @Test
  public void shouldNotMapProtectionTypRedundant() {
    assertThat(ProtectionType.REDUNDANT.getMtosiName(), nullValue());
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
