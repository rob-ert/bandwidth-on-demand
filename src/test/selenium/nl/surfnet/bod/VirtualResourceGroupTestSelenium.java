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
package nl.surfnet.bod;

import static org.junit.Assert.assertTrue;
import nl.surfnet.bod.domain.VirtualResourceGroup;
import nl.surfnet.bod.pages.virtualresourcegroup.NewVirtualResourceGroupPage;
import nl.surfnet.bod.support.TestExternalSupport;
import nl.surfnet.bod.support.VirtualResourceGroupFactory;

import org.junit.Before;
import org.junit.Test;

public class VirtualResourceGroupTestSelenium extends TestExternalSupport {

  @Before
  public void setUp() {
    getWebDriver().performLogin("TestUser");
  }

  @Test
  public void createVirtualResourceGroup() throws Exception {
    givenAVirtualResourceGroup("vrg1");

    getWebDriver().verifyVirtualResourceGroupWasCreated("vrg1");
  }

  @Test
  public void createExistingVirtualResourceGroup() throws Exception {
    givenAVirtualResourceGroup("vrg2");
    getWebDriver().verifyVirtualResourceGroupWasCreated("vrg2");

    VirtualResourceGroup vrg = new VirtualResourceGroupFactory().setSurfConnextGroupName("vrg2").create();
    NewVirtualResourceGroupPage virtualResourceGroupPage = getWebDriver().createNewVirtualResourceGroup(
        vrg.getSurfConnextGroupName());

    assertTrue(virtualResourceGroupPage.hasErrorSurfConnextGroupName());    
  }

  @Test
  public void deleteVirtualResourceGroup() throws Exception {
    VirtualResourceGroup vrg = givenAVirtualResourceGroup("vrgToDelete");

    getWebDriver().deleteVirtualResourceGroup(vrg);

    getWebDriver().verifyVirtualResourceGroupWasDeleted(vrg);
  }

  private VirtualResourceGroup givenAVirtualResourceGroup(String groupName) throws Exception {
    VirtualResourceGroup vrg = new VirtualResourceGroupFactory().setSurfConnextGroupName(groupName).create();

    getWebDriver().createNewVirtualResourceGroup(vrg.getSurfConnextGroupName());

    return vrg;
  }
}
