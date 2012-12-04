/**
 * Copyright (c) 2012, SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod.nbi;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

import org.junit.Test;

import nl.surfnet.bod.domain.PhysicalPort;

public class NbiOfflineClientTest {

  private NbiOfflineClient subject = new NbiOfflineClient();

  @Test
  public void offlineClientShouldGivePorts() {
    List<PhysicalPort> ports = subject.findAllPhysicalPorts();

    assertThat(ports, hasSize(greaterThan(0)));
  }

  @Test
  public void countShouldMatchNumberOfPorts() {
    List<PhysicalPort> ports = subject.findAllPhysicalPorts();
    long count = subject.getPhysicalPortsCount();

    assertThat(count, is((long) ports.size()));
  }

  @Test
  public void findByNmsPortId() {
    PhysicalPort port = subject.findAllPhysicalPorts().get(0);

    PhysicalPort foundPort = subject.findPhysicalPortByNmsPortId(port.getNmsPortId());

    assertThat(foundPort.getNmsPortId(), is(port.getNmsPortId()));
  }

  @Test
  public void testRequireVlanIdWhenPortIdContainsLabel() {
    for (PhysicalPort port : subject.findAllPhysicalPorts()) {
      assertThat(port.toString(), port.isVlanRequired(),
          not(port.getBodPortId().toLowerCase().contains(NbiClient.VLAN_REQUIRED_SELECTOR)));
    }
  }

}
