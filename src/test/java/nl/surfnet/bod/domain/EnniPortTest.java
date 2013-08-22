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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import nl.surfnet.bod.nsi.NsiConstants;

import org.junit.Test;

public class EnniPortTest {

  @Test
  public void should_not_allow_vlan_for_epl_port() {
    EnniPort subject = epl();
    subject.setVlanRanges("1-1000");

    assertThat(subject.isVlanIdAllowed(1), is(false));
  }

  @Test
  public void should_support_vlan_ranges() {
    EnniPort subject = evpl("1,100-1000,2000-2001");
    assertThat(subject.isVlanIdAllowed(1), is(true));
    assertThat(subject.isVlanIdAllowed(2), is(false));
    assertThat(subject.isVlanIdAllowed(99), is(false));
    assertThat(subject.isVlanIdAllowed(100), is(true));
    assertThat(subject.isVlanIdAllowed(101), is(true));
    assertThat(subject.isVlanIdAllowed(999), is(true));
    assertThat(subject.isVlanIdAllowed(1000), is(true));
    assertThat(subject.isVlanIdAllowed(1001), is(false));
    assertThat(subject.isVlanIdAllowed(1999), is(false));
    assertThat(subject.isVlanIdAllowed(2000), is(true));
    assertThat(subject.isVlanIdAllowed(2001), is(true));
    assertThat(subject.isVlanIdAllowed(2002), is(false));
  }

  @Test
  public void should_derive_nsi_v2_stp_from_bod_port_id() {
    EnniPort subject = new EnniPort();
    subject.setBodPortId("bod-port-id");
    assertThat(subject.getNsiStpIdV2(), is(NsiConstants.URN_STP_V2 + ":bod-port-id"));
  }

  private EnniPort evpl(String vlanRanges) {
    NbiPort nbiPort = new NbiPort();
    nbiPort.setVlanRequired(true);
    EnniPort subject = new EnniPort(nbiPort);
    subject.setVlanRanges(vlanRanges);
    return subject;
  }

  private EnniPort epl() {
    NbiPort nbiPort = new NbiPort();
    nbiPort.setVlanRequired(false);
    EnniPort subject = new EnniPort(nbiPort);
    return subject;
  }
}
