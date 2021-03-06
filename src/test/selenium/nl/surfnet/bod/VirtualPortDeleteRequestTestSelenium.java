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

import nl.surfnet.bod.service.DatabaseTestHelper;
import nl.surfnet.bod.support.SeleniumWithSingleSetup;

import org.junit.After;
import org.junit.Test;

public class VirtualPortDeleteRequestTestSelenium extends SeleniumWithSingleSetup {

  @Override
  public void setupInitialData() {
    getNocDriver().createNewApiBasedPhysicalResourceGroup(GROUP_SURFNET, ICT_MANAGERS_GROUP_2, "test@test.nl");
    getNocDriver().linkUniPort(NMS_NOVLAN_PORT_ID_1, "PhysicalPort One", GROUP_SURFNET);

    getWebDriver().clickLinkInLastEmail();
  }

  @After
  public void clearVirtualPorts() {
    DatabaseTestHelper.deleteVirtualPortsFromSeleniumDatabase();
  }

  @Test
  public void requestDeleteVirtualPort() {
    getManagerDriver().switchToUserRole();

    getUserDriver().selectTeamInstituteAndRequest("Selenium users", GROUP_SURFNET, "PortToDelete", 1000, "Doe mijn een nieuw poort...");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().acceptVirtualPort("PhysicalPort One", "PortToDelete", Optional.of("PortToDelete"), Optional.<Integer>absent());
    getManagerDriver().switchToUserRole();

    getUserDriver().requestDeleteVirtualPort("PortToDelete", "Port not needed anymore");

    getWebDriver().clickLinkInLastEmail();

    getManagerDriver().verifyDeleteVirtualPortRequest("Port not needed anymore", "PortToDelete", "PhysicalPort One");

    getManagerDriver().acceptDeleteVirtualPort();

    getManagerDriver().switchToUserRole();

    getUserDriver().verifyNoMoreVirtualPorts();
  }

}
