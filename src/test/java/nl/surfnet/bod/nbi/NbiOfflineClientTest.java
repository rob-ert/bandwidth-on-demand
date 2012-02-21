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
import nl.surfnet.bod.nbi.NbiOfflineClient;

import org.junit.Test;

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
  public void findByNetworkElementId() {
    PhysicalPort port = subject.findAllPhysicalPorts().get(0);

    PhysicalPort foundPort = subject.findPhysicalPortByNetworkElementId(port.getNetworkElementPk());

    assertThat(foundPort.getNetworkElementPk(), is(port.getNetworkElementPk()));
  }

}
