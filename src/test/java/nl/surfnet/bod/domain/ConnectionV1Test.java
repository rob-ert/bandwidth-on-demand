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
import nl.surfnet.bod.domain.ConnectionV1.PathTypeUserType;
import nl.surfnet.bod.domain.ConnectionV1.ServiceParametersTypeUserType;
import nl.surfnet.bod.support.ConnectionV1Factory;
import nl.surfnet.bod.support.ReservationFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;

import org.junit.Test;
import org.ogf.schemas.nsi._2011._10.connection.types.BandwidthType;
import org.ogf.schemas.nsi._2011._10.connection.types.DirectionalityType;
import org.ogf.schemas.nsi._2011._10.connection.types.PathType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceTerminationPointType;

public class ConnectionV1Test {

  @Test
  public void getAdminGroupsWhenConnectionHasNoReservation() {
    Connection subject = new ConnectionV1Factory().setReservation(null).create();

    assertThat(subject.getAdminGroups(), hasSize(0));
  }

  @Test
  public void getAdminGroupsShouldHaveReservationsAdminGroups() {
    VirtualResourceGroup vrg = new VirtualResourceGroupFactory().create();
    VirtualPort source = new VirtualPortFactory().setPhysicalPortAdminGroup("urn:managers1").setVirtualResourceGroup(vrg).create();
    VirtualPort destination = new VirtualPortFactory().setPhysicalPortAdminGroup("urn:managers2").setVirtualResourceGroup(vrg).create();
    Reservation reservation = new ReservationFactory().setSourcePort(source).setDestinationPort(destination).create();
    Connection subject = new ConnectionV1Factory().setReservation(reservation).create();

    assertThat(subject.getAdminGroups(), hasSize(3));
  }

  private static final String PATH_TYPE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
      + "<ns5:path xmlns:ns2=\"urn:oasis:names:tc:SAML:2.0:assertion\" "
      + "xmlns:ns4=\"http://www.w3.org/2000/09/xmldsig#\" "
      + "xmlns:ns3=\"http://www.w3.org/2001/04/xmlenc#\" "
      + "xmlns:ns5=\"http://schemas.ogf.org/nsi/2011/10/connection/types\">"
      + "<directionality>Bidirectional</directionality><destSTP><stpId>stp-id</stpId></destSTP></ns5:path>";

  @Test
  public void should_deserialize_path_type_from_xml_string() {
    PathType result = new PathTypeUserType().fromXmlString(PATH_TYPE_XML);

    assertNotNull(result);
    assertThat(result.getDirectionality(), is(DirectionalityType.BIDIRECTIONAL));
    assertThat(result.getDestSTP().getStpId(), is("stp-id"));
  }

  @Test
  public void should_serialize_path_type_to_xml_string() {
    PathType path = new PathType();
    path.setDirectionality(DirectionalityType.BIDIRECTIONAL);
    ServiceTerminationPointType serviceTerminationPointType = new ServiceTerminationPointType();
    serviceTerminationPointType.setStpId("stp-id");
    path.setDestSTP(serviceTerminationPointType);

    String string = new PathTypeUserType().toXmlString(path);

    assertThat(string, is(PATH_TYPE_XML));
  }

  private static final String SERVICE_PARAMTERS_TYPE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
      + "<ns5:serviceParameters xmlns:ns2=\"urn:oasis:names:tc:SAML:2.0:assertion\" "
      + "xmlns:ns4=\"http://www.w3.org/2000/09/xmldsig#\" "
      + "xmlns:ns3=\"http://www.w3.org/2001/04/xmlenc#\" "
      + "xmlns:ns5=\"http://schemas.ogf.org/nsi/2011/10/connection/types\">"
      + "<bandwidth><desired>100</desired></bandwidth></ns5:serviceParameters>";

  @Test
  public void should_deserialize_service_parameters_from_xml_string() {
    ServiceParametersType result = new ServiceParametersTypeUserType().fromXmlString(SERVICE_PARAMTERS_TYPE_XML);

    assertNotNull(result);
    assertThat(result.getBandwidth().getDesired(), is(100));
  }

  @Test
  public void should_serialize_service_parameters_to_xml_string() {
    BandwidthType bandwidth = new BandwidthType();
    bandwidth.setDesired(100);
    ServiceParametersType parameters = new ServiceParametersType();
    parameters.setBandwidth(bandwidth);

    String xml = new ServiceParametersTypeUserType().toXmlString(parameters);

    assertThat(xml, is(SERVICE_PARAMTERS_TYPE_XML));
  }

}
