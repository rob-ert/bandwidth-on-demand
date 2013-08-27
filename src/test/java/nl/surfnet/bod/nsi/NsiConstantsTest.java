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
package nl.surfnet.bod.nsi;

import static nl.surfnet.bod.nsi.NsiConstants.isValidNurn;
import static nl.surfnet.bod.nsi.NsiConstants.parseLocalNsiId;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import nl.surfnet.bod.domain.NsiVersion;

import org.junit.Test;

public class NsiConstantsTest {

  @Test
  public void should_match_enni_port_id() {
    assertThat(parseLocalNsiId("urn:ogf:network:surfnet.nl:1990:Mock_Ut002A_OME01_ETH-1-1-4", NsiVersion.TWO), is("Mock_Ut002A_OME01_ETH-1-1-4"));
    assertThat(parseLocalNsiId("urn:ogf:network:surfnet.nl:1990:azAZ09+,-.:;=_!$()*@~&", NsiVersion.TWO), is("azAZ09+,-.:;=_!$()*@~&"));
    assertThat(parseLocalNsiId("urn:ogf:network:surfnet.nl:1990:", NsiVersion.TWO), is(""));

    assertThat(parseLocalNsiId("urn:ogf:network:surfnet.nl:1990:#", NsiVersion.TWO), is(nullValue()));
    assertThat(parseLocalNsiId("urn:ogf:network:surfnet.nl:1990:%33", NsiVersion.TWO), is(nullValue()));
    assertThat(parseLocalNsiId("urn:ogf:network:surfnet.nl:1990:'", NsiVersion.TWO), is(nullValue()));
    assertThat(parseLocalNsiId("urn:ogf:network:surfnet.nl:1990:<", NsiVersion.TWO), is(nullValue()));
  }

  @Test
  public void testNurnPattern() {
    assertTrue(isValidNurn("urn:ogf:network:surfnet.nl:1990:Asd001A_OME3T_ETH-1-1-4_10:in"));
    assertTrue(isValidNurn("urn:ogf:network:surfnet.nl:1990:Asd001A_OME3T_ETH-1-1-4_10:in"));
    assertTrue(isValidNurn("urn:ogf:network:surfnet.nl:1990:Asd001A_OME3T_ETH-1-1-4_10:in?foo#bar"));

    assertFalse(isValidNurn("urn:foo"));
    assertFalse(isValidNurn("urn:ogf:network:surfnet.nl:Asd001A_OME3T_ETH-1-1-4_10:in"));
    assertFalse(isValidNurn("urn:ogf:network:surfnet.nl:a1990:Asd001A_OME3T_ETH-1-1-4_10:in"));
    assertFalse(isValidNurn("urn:ogf:network:žurfnet.nl:1990:Asd001A_OME3T_ETH-1-1-4_10:in"));

  }
}
