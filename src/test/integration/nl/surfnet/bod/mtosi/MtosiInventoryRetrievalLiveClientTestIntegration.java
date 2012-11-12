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
package nl.surfnet.bod.mtosi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import nl.surfnet.bod.domain.PhysicalPort;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class MtosiInventoryRetrievalLiveClientTestIntegration {

  private final Properties properties = new Properties();

  private MtosiInventoryRetrievalLiveClient mtosiInventoryRetrievalLiveClient;

  @Before
  public void setup() throws IOException {
    properties.load(ClassLoader.class.getResourceAsStream("/bod-default.properties"));
    mtosiInventoryRetrievalLiveClient = new MtosiInventoryRetrievalLiveClient(properties.get(
        "mtosi.inventory.retrieval.endpoint").toString(), properties.get("mtosi.inventory.sender.uri").toString());
  }

  @Ignore("Currently returns 0 NE's")
  @Test
  public void getUnallocatedPorts() {
    final List<PhysicalPort> unallocatedPorts = mtosiInventoryRetrievalLiveClient.getUnallocatedPorts();
    assertThat(unallocatedPorts, hasSize(greaterThan(0)));
    final PhysicalPort firstPhysicalPort = unallocatedPorts.get(0);

    // It's always /rack=1/shelf=1 for every NE so we can use 1-1 safely
    assertThat(firstPhysicalPort.getBodPortId(), startsWith("SAP-"));
    assertThat(firstPhysicalPort.getNmsPortId(), containsString("1-1"));
    assertThat(firstPhysicalPort.getNmsPortSpeed(), notNullValue());
    assertThat(firstPhysicalPort.getNmsSapName(), startsWith("SAP-"));
    assertThat(firstPhysicalPort.getNmsSapName(), equalTo(firstPhysicalPort.getBodPortId()));
    assertThat(firstPhysicalPort.isAlignedWithNMS(), is(true));

  }

  @Ignore("Currently returns 0 NE's")
  @Test
  public void getUnallocatedPortsCount() {
    assertThat(mtosiInventoryRetrievalLiveClient.getUnallocatedMtosiPortCount(), greaterThan(0));
  }

}
