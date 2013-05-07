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
package nl.surfnet.bod.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.ogf.schemas.nsi._2011._10.connection.types.BandwidthType;
import org.ogf.schemas.nsi._2011._10.connection.types.ServiceParametersType;

public class ServiceParametersTypeUserTypeTest {

  private String SERVICE_PARAMTERS_TYPE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
      + "<ns5:serviceParameters xmlns:ns2=\"urn:oasis:names:tc:SAML:2.0:assertion\" xmlns:ns4=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:ns3=\"http://www.w3.org/2001/04/xmlenc#\" xmlns:ns5=\"http://schemas.ogf.org/nsi/2011/10/connection/types\">"
      + "<bandwidth><desired>100</desired></bandwidth></ns5:serviceParameters>";
  private ServiceParametersTypeUserType subject = new ServiceParametersTypeUserType();

  @Test
  public void shouldDeserializeFromXmlString() {
    ServiceParametersType result = subject.fromXmlString(SERVICE_PARAMTERS_TYPE_XML);

    assertNotNull(result);
    assertThat(result.getBandwidth().getDesired(), is(100));
  }

  @Test
  public void shouldSerializeToXmlString() {
    BandwidthType bandwidth = new BandwidthType();
    bandwidth.setDesired(100);
    ServiceParametersType parameters = new ServiceParametersType();
    parameters.setBandwidth(bandwidth);

    String xml = subject.toXmlString(parameters);

    assertThat(xml, is(SERVICE_PARAMTERS_TYPE_XML));
  }
}
