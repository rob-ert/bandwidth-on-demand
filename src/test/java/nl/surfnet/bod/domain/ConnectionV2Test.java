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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import nl.surfnet.bod.domain.ConnectionV2.PathTypeUserType;
import nl.surfnet.bod.domain.ConnectionV2.ServiceAttributesUserType;

import org.junit.Test;
import org.ogf.schemas.nsi._2013._04.connection.types.DirectionalityType;
import org.ogf.schemas.nsi._2013._04.connection.types.PathType;
import org.ogf.schemas.nsi._2013._04.connection.types.StpType;
import org.ogf.schemas.nsi._2013._04.framework.types.TypeValuePairListType;
import org.ogf.schemas.nsi._2013._04.framework.types.TypeValuePairType;

public class ConnectionV2Test {

  private static final String PATH_TYPE_XML =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
    + "<ns2:path xmlns:ns2=\"http://schemas.ogf.org/nsi/2013/04/connection/types\">"
    + "<directionality>Bidirectional</directionality><symmetricPath>true</symmetricPath>"
    + "<sourceSTP><networkId>surfnet.nl</networkId><localId>1</localId></sourceSTP>"
    + "<destSTP><networkId>surfnet.nl</networkId><localId>2</localId></destSTP></ns2:path>";

  @Test
  public void should_deserialize_path_type_from_xml_string() {
    PathType result = new PathTypeUserType().fromXmlString(PATH_TYPE_XML);

    assertNotNull(result);
    assertThat(result.getDestSTP().getNetworkId(), is("surfnet.nl"));
    assertThat(result.getDestSTP().getLocalId(), is("2"));
    assertThat(result.getDirectionality(), is(DirectionalityType.BIDIRECTIONAL));
  }

  @Test
  public void should_serialize_path_type_to_xml_string() {
    PathType path = new PathType()
      .withSourceSTP(new StpType().withNetworkId("surfnet.nl").withLocalId("1"))
      .withDestSTP(new StpType().withNetworkId("surfnet.nl").withLocalId("2"))
      .withSymmetricPath(true)
      .withDirectionality(DirectionalityType.BIDIRECTIONAL);

    String xml = new PathTypeUserType().toXmlString(path);

    assertThat(xml, is(PATH_TYPE_XML));
  }

  private static final String SERVICE_ATTRIBUTES_TYPE_XML =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
    + "<ns3:serviceAttributes xmlns:ns2=\"http://schemas.ogf.org/nsi/2013/04/connection/types\" "
    + "xmlns:ns3=\"http://schemas.ogf.org/nsi/2013/04/framework/types\">"
    + "<attribute type=\"Type\"><value>Value</value></attribute></ns3:serviceAttributes>";

  @Test
  public void should_deserialize_servcice_parameters_from_xml_string() {
    TypeValuePairListType serviceAttributes = new ServiceAttributesUserType().fromXmlString(SERVICE_ATTRIBUTES_TYPE_XML);

    assertNotNull(serviceAttributes);
    assertThat(serviceAttributes.getAttribute(), hasSize(1));
    assertThat(serviceAttributes.getAttribute().get(0).getType(), is("Type"));
    assertThat(serviceAttributes.getAttribute().get(0).getValue(), hasSize(1));
    assertThat(serviceAttributes.getAttribute().get(0).getValue().get(0), is("Value"));
  }

  @Test
  public void should_serialize_service_parameters_to_xml_string() {
    TypeValuePairListType serviceParameters = new TypeValuePairListType().withAttribute(new TypeValuePairType().withValue("Value").withType("Type"));

    String xml = new ServiceAttributesUserType().toXmlString(serviceParameters);

    assertThat(xml, is(SERVICE_ATTRIBUTES_TYPE_XML));
  }
}
