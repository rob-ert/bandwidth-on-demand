/**
 * The owner of the original code is SURFnet BV.
 *
 * Portions created by the original owner are Copyright (C) 2011-2012 the
 * original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of the SURFnet7 Bandwidth on Demand software.
 *
 * The SURFnet7 Bandwidth on Demand software is free software: you can
 * redistribute it and/or modify it under the terms of the BSD license
 * included with this distribution.
 *
 * If the BSD license cannot be found with this distribution, it is available
 * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
 */
package nl.surfnet.bod.nbi;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import nl.surfnet.bod.domain.PhysicalPort;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/appCtx-nbi-client.xml", "classpath:/spring/appCtx.xml",
    "classpath:/spring/appCtx-jpa-integration.xml","classpath:/spring/appCtx-idd-client.xml" })
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
