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

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import nl.surfnet.bod.domain.PhysicalPort;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/appCtx-nbi-client.xml", "classpath:/spring/appCtx.xml",
    "classpath:/spring/appCtx-jpa-integration.xml","classpath:/spring/appCtx-idd-client.xml", "/spring/appCtx-vers-client.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class NbiClientTestIntegration {

  @Resource
  private NbiClient nbiClient;

  @Test
  public void testFindAllPhysicalPorts() throws Exception {
    List<PhysicalPort> allPorts = nbiClient.findAllPhysicalPorts();
    assertThat(allPorts, hasSize(greaterThan(0)));
  }

  @Test
  public void testFindPhysicalPortByNmsPortId() throws Exception {
    PhysicalPort firstPort = nbiClient.findAllPhysicalPorts().get(0);

    PhysicalPort foundPort = nbiClient.findPhysicalPortByNmsPortId(firstPort.getNmsPortId());

    assertThat(foundPort.getNmsPortId(), is(firstPort.getNmsPortId()));
  }

  @Test
  public void portCountShouldMatchSizeOfAllPorts() {
    long count = nbiClient.getPhysicalPortsCount();
    List<PhysicalPort> ports = nbiClient.findAllPhysicalPorts();

    assertThat(count, is((long) ports.size()));
  }

  @Test
  public void testRequireVlanIdWhenPortIdContainsLabel() {
    for (PhysicalPort port : nbiClient.findAllPhysicalPorts()) {
      assertThat(port.toString(), port.isVlanRequired(),
          not(port.getBodPortId().toLowerCase().contains(NbiClient.VLAN_REQUIRED_SELECTOR)));
    }
  }

}
