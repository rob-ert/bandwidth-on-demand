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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.List;

import nl.surfnet.bod.domain.PhysicalPort;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/spring/bod-opendrac-test.xml")
public class NbiClientTestIntegration {

  @Autowired
  private NbiClient nbiClient;

  @Test
  public void testFindAllPhysicalPorts() throws Exception {
    List<PhysicalPort> allPorts = nbiClient.findAllPhysicalPorts();
    assertThat(allPorts, hasSize(greaterThan(0)));
  }

  @Test
  public void testFindPhysicalPortByNetworkElementId() throws Exception {
    PhysicalPort firstPort = nbiClient.findAllPhysicalPorts().get(0);

    PhysicalPort foundPort = nbiClient.findPhysicalPortByNetworkElementId(firstPort.getNetworkElementPk());

    assertThat(foundPort.getNetworkElementPk(), is(firstPort.getNetworkElementPk()));
  }

  @Test
  public void portCountShouldMatchSizeOfAllPorts() {
    long count = nbiClient.getPhysicalPortsCount();
    List<PhysicalPort> ports = nbiClient.findAllPhysicalPorts();

    assertThat(count, is((long) ports.size()));
  }

}
