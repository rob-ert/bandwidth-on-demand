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

import static nl.surfnet.bod.matchers.OptionalMatchers.isAbsent;
import static nl.surfnet.bod.matchers.OptionalMatchers.isPresent;
import static nl.surfnet.bod.nsi.NsiHelper.NURN_PATTERN_REGEXP;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import nl.surfnet.bod.domain.EnniPort;
import nl.surfnet.bod.domain.NsiVersion;

import org.junit.Test;


public class NsiHelperTest {

  private NsiHelper subject = new NsiHelper("surfnet.nl", "surfnet.nl:1990", "bod", "production6", "");

  @Test
  public void should_derive_nsi_v2_stp_from_bod_port_id() {
    EnniPort port = new EnniPort();
    port.setBodPortId("bod-port-id");

    assertThat(subject.getStpIdV2(port), is("urn:ogf:network:surfnet.nl:1990:production6:bod-port-id"));
  }

  @Test
  public void should_derive_nsi_v1_stp_from_bod_port_id() {
    EnniPort port = new EnniPort();
    port.setBodPortId("bod-port-id");

    assertThat(subject.getStpIdV1(port), is("urn:ogf:network:stp:surfnet.nl:bod-port-id"));
  }

  @Test
  public void should_match_enni_port_id() {
    assertThat(subject.parseLocalNsiId("urn:ogf:network:surfnet.nl:1990:production6:Mock_Ut002A_OME01_ETH-1-1-4", NsiVersion.TWO), isPresent("Mock_Ut002A_OME01_ETH-1-1-4"));
    assertThat(subject.parseLocalNsiId("urn:ogf:network:surfnet.nl:1990:production6:azAZ09+,-.:;=_!$()*@~&", NsiVersion.TWO), isPresent("azAZ09+,-.:;=_!$()*@~&"));
    assertThat(subject.parseLocalNsiId("urn:ogf:network:surfnet.nl:1990:production6:22?vlan=44", NsiVersion.TWO), isPresent("22"));

    assertThat(subject.parseLocalNsiId("urn:ogf:network:surfnet.nl:1990:production6:", NsiVersion.TWO), isAbsent());
    assertThat(subject.parseLocalNsiId("urn:ogf:network:surfnet.nl:1990:production6:#", NsiVersion.TWO), isAbsent());
    assertThat(subject.parseLocalNsiId("urn:ogf:network:surfnet.nl:1990:production6:%33", NsiVersion.TWO), isAbsent());
    assertThat(subject.parseLocalNsiId("urn:ogf:network:surfnet.nl:1990:production6:'", NsiVersion.TWO), isAbsent());
    assertThat(subject.parseLocalNsiId("urn:ogf:network:surfnet.nl:1990:production6:<", NsiVersion.TWO), isAbsent());
  }

  @Test
  public void testNurnPattern() {
    assertTrue(Pattern.matches(NURN_PATTERN_REGEXP, "urn:ogf:network:surfnet.nl:1990:Asd001A_OME3T_ETH-1-1-4_10:in"));
    assertTrue(Pattern.matches(NURN_PATTERN_REGEXP, "urn:ogf:network:surfnet.nl:1990:Asd001A_OME3T_ETH-1-1-4_10:in"));
    assertTrue(Pattern.matches(NURN_PATTERN_REGEXP, "urn:ogf:network:surfnet.nl:1990:Asd001A_OME3T_ETH-1-1-4_10:in?foo#bar"));

    assertFalse(Pattern.matches(NURN_PATTERN_REGEXP, "urn:foo"));
    assertFalse(Pattern.matches(NURN_PATTERN_REGEXP, "urn:ogf:network:surfnet.nl:Asd001A_OME3T_ETH-1-1-4_10:in"));
    assertFalse(Pattern.matches(NURN_PATTERN_REGEXP, "urn:ogf:network:surfnet.nl:a1990:Asd001A_OME3T_ETH-1-1-4_10:in"));
    assertFalse(Pattern.matches(NURN_PATTERN_REGEXP, "urn:ogf:network:žurfnet.nl:1990:Asd001A_OME3T_ETH-1-1-4_10:in"));
  }

  @Test
  public void should_derive_urn_topology() {
    assertThat(subject.getUrnTopology(), is("urn:ogf:network:surfnet.nl:1990:topology:production6"));
  }

}