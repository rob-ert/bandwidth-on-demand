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
package nl.surfnet.bod.domain;

import static org.junit.Assert.assertEquals;
import nl.surfnet.bod.support.PhysicalPortFactory;
import nl.surfnet.bod.support.VirtualPortFactory;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;

import org.junit.Before;
import org.junit.Test;

public class VirtualPortTest {

  private static final String PHYSICAL_PORT_NAME = "portName";
  private PhysicalPort physicalPort;
  private VirtualResourceGroup virtualResourceGroup;

  @Before
  public void setUp() {
    physicalPort = new PhysicalPortFactory().setName(PHYSICAL_PORT_NAME).create();
    virtualResourceGroup = new VirtualResourceGroupFactory().create();
  }

  @Test
  public void testSetters() {
    VirtualPort vPort = new VirtualPortFactory().setName("vPortName").setPhysicalPort(physicalPort).setVirtualResourceGroup(virtualResourceGroup). create();

    assertEquals(vPort.getPhysicalPort(), physicalPort);
    assertEquals(vPort.getVirtualResourceGroup(), virtualResourceGroup);
  }
}
