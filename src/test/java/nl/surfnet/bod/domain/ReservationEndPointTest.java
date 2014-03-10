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

import java.util.Optional;

import nl.surfnet.bod.support.NbiPortFactory;
import nl.surfnet.bod.support.VirtualPortFactory;

import org.hamcrest.Matchers;
import org.junit.Test;



public class ReservationEndPointTest {

  @Test
  public void should_support_virtual_port_as_end_point() {
    VirtualPort virtualPort = new VirtualPortFactory().create();
    ReservationEndPoint subject = new ReservationEndPoint(virtualPort);

    assertThat(subject.getVirtualPort(), is(Optional.of(virtualPort)));
    assertThat(subject.getEnniPort(), is(Optional.empty()));
    assertThat(subject.getUniPort(), is(Optional.of(virtualPort.getPhysicalPort())));
    assertThat(subject.getPhysicalPort(), Matchers.<PhysicalPort>is(virtualPort.getPhysicalPort()));
  }

  @Test
  public void should_support_enni_port_with_vlan_as_end_point() {
    EnniPort enniPort = new EnniPort(new NbiPortFactory().setVlanRequired(true).create());
    ReservationEndPoint subject = new ReservationEndPoint(enniPort, Optional.of(2));

    assertThat(subject.getEnniPort(), is(Optional.of(enniPort)));
    assertThat(subject.getEnniVlanId(), is(Optional.of(2)));
    assertThat(subject.getVirtualPort(), is(Optional.empty()));
    assertThat(subject.getPhysicalPort(), Matchers.<PhysicalPort>is(enniPort));
  }

  @Test
  public void should_support_enni_port_without_vlan_as_end_point() {
    EnniPort enniPort = new EnniPort(new NbiPortFactory().setVlanRequired(false).create());
    ReservationEndPoint subject = new ReservationEndPoint(enniPort, Optional.empty());

    assertThat(subject.getEnniPort(), is(Optional.of(enniPort)));
    assertThat(subject.getEnniVlanId(), is(Optional.empty()));
    assertThat(subject.getVirtualPort(), is(Optional.empty()));
  }

}
