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
package nl.surfnet.bod;

import nl.surfnet.bod.support.TestExternalSupport;

import org.junit.Before;
import org.junit.Test;

public class VirtualPortTestSelenium extends TestExternalSupport {

  private static final String VP_DELETE_ALERT_TEXT = "reservations will be effected";

  @Before
  public void setup() {
    getNocDriver().createNewPhysicalResourceGroup("SIDN", ICT_MANAGERS_GROUP, "test@test.nl");
    getNocDriver().linkPhysicalPort(NMS_PORT_ID_2, "Request a virtual port", "SIDN");

    getWebDriver().clickLinkInLastEmail();

    getUserDriver().requestVirtualPort("selenium-users");
    getUserDriver().selectInstituteAndRequest("SIDN", 1000, "port 1");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().createVirtualPort("First port");

    getUserDriver().requestVirtualPort("selenium-users");
    getUserDriver().selectInstituteAndRequest("SIDN", 1000, "port 2");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().createVirtualPort("Second port");
  }

  @Test
  public void lastPortDeletedShouldDeleteVirtualResourceGroup() {
    getManagerDriver().verifyVirtualResourceGroupExists("selenium-users", "2");

    getManagerDriver().deleteVirtualPortAndVerifyAlertText("First port", VP_DELETE_ALERT_TEXT);

    getManagerDriver().switchToUser();

    getUserDriver().verifyMemberOf("selenium-users");

    getUserDriver().switchToManager("SIDN");
    getManagerDriver().verifyVirtualResourceGroupExists("selenium-users", "1");

    getManagerDriver().switchToNoc();
    getNocDriver().verifyVirtualResourceGroupExists("selenium-users", "1");

    getUserDriver().switchToManager("SIDN");
    getManagerDriver().deleteVirtualPortAndVerifyAlertText("Second port", VP_DELETE_ALERT_TEXT);

    getManagerDriver().verifyVirtualResourceGroupsEmpty();

    getManagerDriver().switchToUser();

    getUserDriver().verifyNotMemberOf("selenium-users");
  }

  @Test
  public void verifyNocLinkFromTeamToVirtualPorts() {
    getNocDriver().verifyTeamToVirtualPortsLink("selenium-users");
  }

  @Test
  public void verifyManagerLinkFromTeamToVirtualPorts() {
    getManagerDriver().verifyTeamToVirtualPortsLink("selenium-users");
  }

}
