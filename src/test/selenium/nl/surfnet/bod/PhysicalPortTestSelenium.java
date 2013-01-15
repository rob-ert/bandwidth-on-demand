/**
 * Copyright (c) 2012, SURFnet BV
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
package nl.surfnet.bod;

import nl.surfnet.bod.support.TestExternalSupport;

import org.junit.Before;
import org.junit.Test;

public class PhysicalPortTestSelenium extends TestExternalSupport {

  private static final String GROUP_NAME = "SIDN";

  @Before
  public void setup() {
    getNocDriver().createNewPhysicalResourceGroup(GROUP_NAME, ICT_MANAGERS_GROUP, "test@example.com");
  }

  @Test
  public void allocatePhysicalPortFromInstitutePage() {
    getNocDriver().addPhysicalPortToInstitute(GROUP_NAME, "NOC label", "Mock_Poort 1de verdieping toren1a");

    getNocDriver().verifyPhysicalPortWasAllocated(BOD_PORT_ID_1, "NOC label");

    getNocDriver().unlinkPhysicalPort(BOD_PORT_ID_1);
  }

  @Test
  public void createRenameAndDeleteAPhysicalPort() {
    String nocLabel = "My Selenium Port (Noc)";
    String managerLabel1 = "My Selenium Port (Manager 1st)";
    String managerLabel2 = "My Selenium Port (Manager 2nd)";

    getNocDriver().linkPhysicalPort(NMS_PORT_ID_1, nocLabel, managerLabel1, GROUP_NAME);

    getNocDriver().verifyPhysicalPortWasAllocated(BOD_PORT_ID_1, nocLabel);

    getNocDriver().verifyPhysicalPortHasEnabledUnallocateIcon(BOD_PORT_ID_1, nocLabel);

    getNocDriver().gotoEditPhysicalPortAndVerifyManagerLabel(BOD_PORT_ID_1, managerLabel1);

    getNocDriver().switchToManager();

    getManagerDriver().changeManagerLabelOfPhyiscalPort(BOD_PORT_ID_1, managerLabel2);

    getManagerDriver().verifyManagerLabelChanged(BOD_PORT_ID_1, managerLabel2);

    getManagerDriver().switchToNoc();

    getNocDriver().gotoEditPhysicalPortAndVerifyManagerLabel(BOD_PORT_ID_1, managerLabel2);

    getNocDriver().unlinkPhysicalPort(BOD_PORT_ID_1);
  }

  @Test
  public void checkUnallocateState() {
    final String vpOne = "VirtualPort One";
    String nocLabel = "My Selenium Port (Noc)";
    String managerLabel1 = "My Selenium Port (Manager 1st)";
    setupVirtualPort(vpOne, nocLabel, managerLabel1);

    getManagerDriver().verifyPhysicalPortHasDisabeldUnallocateIcon(BOD_PORT_ID_1, managerLabel1, "related");

    getManagerDriver().switchToNoc();
    getNocDriver().verifyPhysicalPortIsNotOnUnallocatedPage(BOD_PORT_ID_1, nocLabel);

    // Delete Vp
    getNocDriver().switchToManager();
    getManagerDriver().deleteVirtualPort(vpOne);

    // After delete VP, the PysicalPort should be able to unallocate
    getManagerDriver().verifyPhysicalPortHasEnabledUnallocateIcon(BOD_PORT_ID_1, managerLabel1);
    getManagerDriver().switchToNoc();
    getNocDriver().unlinkPhysicalPort(BOD_PORT_ID_1);
  }

  @Test
  public void searchPorts() {
    try {
      getNocDriver().addPhysicalPortToInstitute(GROUP_NAME, "NOC 1 label", "Mock_Poort 1de verdieping toren1a");
      getNocDriver().addPhysicalPortToInstitute(GROUP_NAME, "NOC 2 label", "Mock_Poort 2de verdieping toren1b");
      getNocDriver().addPhysicalPortToInstitute(GROUP_NAME, "NOC 3 label", "Mock_Poort 3de verdieping toren1c");

      getNocDriver().verifyAllocatedPortsBySearch("1", BOD_PORT_ID_1, BOD_PORT_ID_2, BOD_PORT_ID_4);
      getNocDriver().verifyAllocatedPortsBySearch("*1*", BOD_PORT_ID_1, BOD_PORT_ID_2, BOD_PORT_ID_4);

      getNocDriver().verifyAllocatedPortsBySearch("'NOC 1 label'", BOD_PORT_ID_1);
      getNocDriver().verifyAllocatedPortsBySearch("'NOC 1'", new String[] {});
      getNocDriver().verifyAllocatedPortsBySearch("'NOC ? label'", BOD_PORT_ID_1, BOD_PORT_ID_2, BOD_PORT_ID_4);
    }
    finally {
      getNocDriver().unlinkPhysicalPort(BOD_PORT_ID_1);
      getNocDriver().unlinkPhysicalPort(BOD_PORT_ID_2);
      getNocDriver().unlinkPhysicalPort(BOD_PORT_ID_4);
    }
  }

  @Test
  public void verifyManagerLinkFromPhysicalPortToVIrtualPorts() {
    final String vpOne = "VirtualPort One";
    final String nocLabel = "My Selenium Port (Noc)";
    final String managerLabel1 = "My Selenium Port (Manager 1st)";
    setupVirtualPort(vpOne, nocLabel, managerLabel1);

    getManagerDriver().verifyPhysicalPortToVirtualPortsLink(managerLabel1, vpOne);
  }

  @Test
  public void verifySorting() {
    try {
      getNocDriver().linkPhysicalPort(NMS_PORT_ID_1, "123NOC", "XYZPort", GROUP_NAME);
      getNocDriver().linkPhysicalPort(NMS_PORT_ID_2, "987NOC", "ABCPort", GROUP_NAME);
      getNocDriver().linkPhysicalPort(NMS_PORT_ID_3, "abcNOC", "abcPort", GROUP_NAME);
      getNocDriver().linkPhysicalPort(NMS_PORT_ID_4, "AbcNOC", "xyzPort", GROUP_NAME);

      getNocDriver().verifyAllocatedPortsBySort("bodPortId", BOD_PORT_ID_4, BOD_PORT_ID_3, BOD_PORT_ID_1, BOD_PORT_ID_2);
      getNocDriver().verifyAllocatedPortsBySort("nocLabel", "123NOC", "987NOC", "AbcNOC", "abcNOC");
    }
    finally {
      getNocDriver().unlinkPhysicalPort(BOD_PORT_ID_1);
      getNocDriver().unlinkPhysicalPort(BOD_PORT_ID_2);
      getNocDriver().unlinkPhysicalPort(BOD_PORT_ID_3);
      getNocDriver().unlinkPhysicalPort(BOD_PORT_ID_4);
    }
  }

  private void setupVirtualPort(String vpOne, String nocLabel, String managerLabel1) {
    getNocDriver().linkPhysicalPort(NMS_PORT_ID_1, nocLabel, managerLabel1, GROUP_NAME);
    getWebDriver().clickLinkInLastEmail();

    getNocDriver().verifyPhysicalPortHasEnabledUnallocateIcon(BOD_PORT_ID_1, nocLabel);

    getNocDriver().switchToManager(GROUP_NAME);
    getManagerDriver().verifyPhysicalPortHasEnabledUnallocateIcon(BOD_PORT_ID_1, managerLabel1);

    // Link a VirtualPort to the PhysicalPort, PhysicalPort cannot be
    // unallocated anymore
    getManagerDriver().switchToUser();
    getUserDriver().requestVirtualPort("selenium-users");
    getUserDriver().selectInstituteAndRequest(GROUP_NAME, 1000, "Doe mijn een nieuw poort...");
    getUserDriver().switchToManager(GROUP_NAME);
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().acceptVirtualPort(vpOne);
  }

}
