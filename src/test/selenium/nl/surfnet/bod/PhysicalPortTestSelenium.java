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

import nl.surfnet.bod.support.TestExternalSupport;

import org.junit.Before;
import org.junit.Test;

public class PhysicalPortTestSelenium extends TestExternalSupport {

  private final static String GROUP_NAME = "2COLLEGE";

  @Before
  public void setup() {
    getNocDriver().createNewPhysicalResourceGroup(GROUP_NAME, ICT_MANAGERS_GROUP, "test@example.com");
  }

  @Test
  public void allocatePhysicalPortFromInstitutePage() {
    getNocDriver().addPhysicalPortToInstitute(GROUP_NAME, "NOC label", "Mock_Poort 1de verdieping toren1a");

    getNocDriver().verifyPhysicalPortWasAllocated(NETWORK_ELEMENT_PK, "NOC label");

    getNocDriver().unlinkPhysicalPort(NETWORK_ELEMENT_PK);
  }

  @Test
  public void createRenameAndDeleteAPhysicalPort() {
    String nocLabel = "My Selenium Port (Noc)";
    String managerLabel1 = "My Selenium Port (Manager 1st)";
    String managerLabel2 = "My Selenium Port (Manager 2nd)";

    getNocDriver().linkPhysicalPort(NETWORK_ELEMENT_PK, nocLabel, managerLabel1, GROUP_NAME);

    getNocDriver().verifyPhysicalPortWasAllocated(NETWORK_ELEMENT_PK, nocLabel);

    getNocDriver().verifyPhysicalPortHasEnabledUnallocateIcon(NETWORK_ELEMENT_PK, nocLabel);

    getNocDriver().gotoEditPhysicalPortAndVerifyManagerLabel(NETWORK_ELEMENT_PK, managerLabel1);

    getNocDriver().switchToManager();

    getManagerDriver().changeManagerLabelOfPhyiscalPort(NETWORK_ELEMENT_PK, managerLabel2);

    getManagerDriver().verifyManagerLabelChanged(NETWORK_ELEMENT_PK, managerLabel2);

    getManagerDriver().switchToNoc();

    getNocDriver().gotoEditPhysicalPortAndVerifyManagerLabel(NETWORK_ELEMENT_PK, managerLabel2);

    getNocDriver().unlinkPhysicalPort(NETWORK_ELEMENT_PK);
  }

  @Test
  public void checkUnallocateState() {
    final String VP_ONE = "VirtualPort One";
    String nocLabel = "My Selenium Port (Noc)";
    String managerLabel1 = "My Selenium Port (Manager 1st)";

    getNocDriver().linkPhysicalPort(NETWORK_ELEMENT_PK, nocLabel, managerLabel1, GROUP_NAME);
    getWebDriver().clickLinkInLastEmail();
    getNocDriver().verifyPhysicalPortHasEnabledUnallocateIcon(NETWORK_ELEMENT_PK, nocLabel);

    getManagerDriver().verifyPhysicalPortHasEnabledUnallocateIcon(NETWORK_ELEMENT_PK, managerLabel1);

    // Link a VirtualPort to the PhysicalPort, PhysicalPort cannot be
    // unallocated anymore
    getManagerDriver().switchToUser();
    getUserDriver().requestVirtualPort("selenium-users");
    getUserDriver().selectInstituteAndRequest(GROUP_NAME, 1000, "Doe mijn een nieuw poort...");
    getUserDriver().switchToManager(GROUP_NAME);
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().acceptVirtualPort(VP_ONE);

    getManagerDriver().verifyPhysicalPortHasDisabeldUnallocateIcon(NETWORK_ELEMENT_PK, managerLabel1, "related");

    getManagerDriver().switchToNoc();
    getNocDriver().verifyPhysicalPortIsNotOnUnallocatedPage(NETWORK_ELEMENT_PK, nocLabel);

    // Delete Vp
    getNocDriver().switchToManager();
    getManagerDriver().deleteVirtualPort(VP_ONE);
    getManagerDriver().switchToNoc();

    // After delete VP, the PysicalPort should be able to unallocate
    getManagerDriver().verifyPhysicalPortHasEnabledUnallocateIcon(NETWORK_ELEMENT_PK, managerLabel1);
    getNocDriver().unlinkPhysicalPort(NETWORK_ELEMENT_PK);
  }

}
