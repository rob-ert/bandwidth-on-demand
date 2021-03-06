/**
 * Copyright (c) 2012, 2013 SURFnet BV
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

import com.google.common.base.Optional;

import nl.surfnet.bod.support.SeleniumWithSingleSetup;

import org.junit.Test;

public class PhysicalPortTestSelenium extends SeleniumWithSingleSetup {

  private String vpOne = "VirtualPort One";
  private String nocLabel = "My Selenium Port (Noc)";
  private String managerLabel1 = "My Selenium Port (Manager 1st)";

  @Override
  public void setupInitialData() {
    getNocDriver().createNewApiBasedPhysicalResourceGroup(GROUP_SURFNET, ICT_MANAGERS_GROUP, "test@example.com");

    getNocDriver().linkUniPort(NMS_NOVLAN_PORT_ID_1, nocLabel, managerLabel1, GROUP_SURFNET);
    getWebDriver().clickLinkInLastEmail();

    getManagerDriver().switchToUserRole();
    getUserDriver().requestVirtualPort("Selenium users");
    getUserDriver().selectInstituteAndRequest(GROUP_SURFNET, 1000, "Doe mijn een nieuw poort...");

    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().acceptVirtualPort(managerLabel1, vpOne, Optional.<String>absent(), Optional.<Integer>absent());
  }

  @Test
  public void allocateAndUnallocatePhysicalPortFromInstitutePage() {
    getNocDriver().addPhysicalPortToInstitute(GROUP_SURFNET, "NOC label", LABEL_PORT_2);

    getNocDriver().verifyUniPortWasAllocated(BOD_PORT_ID_2, "NOC label");

    getNocDriver().unlinkUniPort(BOD_PORT_ID_2);
  }

  @Test
  public void create_rename_delete_uni_port() {
    String nocLabel = "My Selenium Port (Noc)";
    String managerLabel1 = "My Selenium Port (Manager 1st)";
    String managerLabel2 = "My Selenium Port (Manager 2nd)";

    getNocDriver().linkUniPort(NMS_PORT_ID_2, nocLabel, managerLabel1, GROUP_SURFNET);

    getNocDriver().verifyUniPortWasAllocated(BOD_PORT_ID_2, nocLabel);

    getNocDriver().verifyUniPortHasEnabledUnallocateIcon(BOD_PORT_ID_2, nocLabel);

    getNocDriver().gotoEditUniPortAndVerifyManagerLabel(BOD_PORT_ID_2, managerLabel1);

    getNocDriver().switchToManagerRole();

    getManagerDriver().changeManagerLabelOfUniPort(BOD_PORT_ID_2, managerLabel2);

    getManagerDriver().verifyManagerLabelChanged(BOD_PORT_ID_2, managerLabel2);

    getManagerDriver().switchToNocRole();

    getNocDriver().gotoEditUniPortAndVerifyManagerLabel(BOD_PORT_ID_2, managerLabel2);

    getNocDriver().unlinkUniPort(BOD_PORT_ID_2);
  }

  @Test
  public void create_edit_delete_enni_port() {
    getNocDriver().linkEnniPort(NMS_ENNI_PORT_ID_1, "NOC label", "urn:ogf:network:surfnet:1998:go", "urn:ogf:network:es-net:2013:test", "10-1010");

    getNocDriver().verifyEnniPortWasAllocated(BOD_ENNI_PORT_1, "NOC label");

    getNocDriver().editEnniPort(BOD_ENNI_PORT_1, "new label", "urn:ogf:network:bla:1900:yes", "urn:ogf:network:vla:1800:no", "10");

    getNocDriver().verifyEnniPortWasAllocated(BOD_ENNI_PORT_1, "new label");

    getNocDriver().unlinkEnniPort(BOD_ENNI_PORT_1);
  }

  @Test
  public void checkUnallocateState() {
    getNocDriver().verifyPhysicalPortIsNotOnUnallocatedPage(BOD_NOVLAN_PORT_ID_1, nocLabel);
  }

  @Test
  public void verify_manager_link_from_physical_port_to_virtual_ports() {
    getManagerDriver().verifyPhysicalPortToVirtualPortsLink(managerLabel1, vpOne);
  }

}