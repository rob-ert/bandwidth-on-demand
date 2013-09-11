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
package nl.surfnet.bod.domain;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

import nl.surfnet.bod.domain.ConnectionV2.ReservationConfirmCriteriaTypeUserType;
import nl.surfnet.bod.nsi.v2.ConnectionsV2;

import org.junit.Test;
import org.ogf.schemas.nsi._2013._07.connection.types.ReservationConfirmCriteriaType;
import org.ogf.schemas.nsi._2013._07.services.point2point.EthernetBaseType;
import org.ogf.schemas.nsi._2013._07.services.point2point.EthernetVlanType;
import org.ogf.schemas.nsi._2013._07.services.point2point.P2PServiceBaseType;
import org.ogf.schemas.nsi._2013._07.services.types.DirectionalityType;
import org.ogf.schemas.nsi._2013._07.services.types.StpType;
import org.w3c.dom.Element;

public class ConnectionV2Test {

  @Test
  public void should_deserialize_criteria_type_from_xml_string() {

    String criteriaXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><criteria version=\"0\" xmlns:ns2=\"http://schemas.ogf.org/nsi/2013/07/connection/types\" xmlns:ns4=\"http://www.w3.org/2001/04/xmlenc#\" xmlns:ns3=\"urn:oasis:names:tc:SAML:2.0:assertion\" xmlns:ns5=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:ns6=\"http://schemas.ogf.org/nsi/2013/07/framework/types\" xmlns:ns7=\"http://schemas.ogf.org/nsi/2013/07/framework/headers\"><schedule/><ns3:p2ps xmlns:ns2=\"http://schemas.ogf.org/nsi/2013/07/framework/types\" xmlns:ns3=\"http://schemas.ogf.org/nsi/2013/07/services/point2point\"><capacity>0</capacity><directionality>Bidirectional</directionality><symmetricPath>true</symmetricPath><sourceSTP><networkId>surfnet.nl</networkId><localId>1</localId></sourceSTP><destSTP><networkId>surfnet.nl</networkId><localId>2</localId></destSTP></ns3:p2ps></criteria>";
    ReservationConfirmCriteriaType result = new ReservationConfirmCriteriaTypeUserType().fromXmlString(criteriaXml);

    assertNotNull(result);
    Optional<P2PServiceBaseType> service = ConnectionsV2.findPointToPointService(result);
    assertThat(service.isPresent(), is(true));
    assertThat(service.get().getDestSTP().getNetworkId(), is("surfnet.nl"));
    assertThat(service.get().getDestSTP().getLocalId(), is("2"));
    assertThat(service.get().getDirectionality(), is(DirectionalityType.BIDIRECTIONAL));
  }

  @Test
  public void should_serialize_p2p_to_correct_xml_element() throws Exception {
    List<Object> any = new ArrayList<>();
    final P2PServiceBaseType p2p = new P2PServiceBaseType()
        .withSourceSTP(new StpType().withNetworkId("surfnet.nl").withLocalId("1"))
        .withDestSTP(new StpType().withNetworkId("surfnet.nl").withLocalId("2"))
        .withSymmetricPath(true)
        .withDirectionality(DirectionalityType.BIDIRECTIONAL);
    ConnectionsV2.addPointToPointService(any, p2p);
    Element element = (Element) any.get(0);
    assertThat(element.getLocalName(), equalTo("p2ps"));
  }

  @Test
  public void should_serialize_ethernetBaseType_to_correct_xml_element() throws Exception {
    List<Object> any = new ArrayList<>();

    final EthernetBaseType ethernetBaseType = new EthernetBaseType()
        .withSourceSTP(new StpType().withNetworkId("surfnet.nl").withLocalId("1"))
        .withDestSTP(new StpType().withNetworkId("surfnet.nl").withLocalId("2"))
        .withSymmetricPath(true)
        .withDirectionality(DirectionalityType.BIDIRECTIONAL)
        .withMtu(1)
        .withBurstsize(1l);
    ConnectionsV2.addPointToPointService(any, ethernetBaseType);
    Element element = (Element) any.get(0);
    assertThat(element.getLocalName(), equalTo("ets"));
  }

  public void should_serialize_ethernertVlanType_to_correct_xml_element() {
    List<Object> any = new ArrayList<>();

    final EthernetVlanType ethernetVlanType = new EthernetVlanType()
        .withSourceSTP(new StpType().withNetworkId("surfnet.nl").withLocalId("1"))
        .withDestSTP(new StpType().withNetworkId("surfnet.nl").withLocalId("2"))
        .withSymmetricPath(true)
        .withDirectionality(DirectionalityType.BIDIRECTIONAL)
        .withMtu(1)
        .withBurstsize(1l)
        .withSourceVLAN(1)
        .withDestVLAN(2);
    ConnectionsV2.addPointToPointService(any, ethernetVlanType);
    Element element = (Element) any.get(0);
    assertThat(element.getLocalName(), equalTo("evts"));
  }

}
