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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LogEventTestSelenium extends TestExternalSupport {

  protected static final String ICT_USER_GROUP_2 = //
  "urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:selenium-users2";

  private static final String GROUP_NAME_ONE = "SIDN";
  private static final String GROUP_NAME_TWO = "SURFnet bv";
  private static final String GROUP_NAME_FOUR = "ROC Rijn Ijssel";
  private static final String PORT_LABEL_1 = "NOC Port 1";
  private static final String PORT_LABEL_2 = "NOC Port 2";
  private static final String PORT_LABEL_4 = "NOC Port 4";
  private static final String VP_LABEL_1 = "VP Port 1";
  private static final String VP_LABEL_2 = "VP Port 2";
  private static final String VP_LABEL_4 = "VP Port 4";

  @Before
  public void prepareUser() {
    getNocDriver().createNewPhysicalResourceGroup(GROUP_NAME_ONE, ICT_MANAGERS_GROUP, "test@test.nl");
    getWebDriver().clickLinkInLastEmail();

    getNocDriver().createNewPhysicalResourceGroup(GROUP_NAME_TWO, ICT_MANAGERS_GROUP_2, "test2@test.nl");
    getWebDriver().clickLinkInLastEmail();

    getNocDriver().createNewPhysicalResourceGroup(GROUP_NAME_FOUR, ICT_USER_GROUP_2, "test4@test.nl");
    getWebDriver().clickLinkInLastEmail();

    getNocDriver().linkPhysicalPort(NMS_PORT_ID_1, PORT_LABEL_1, GROUP_NAME_ONE);
    getNocDriver().linkPhysicalPort(NMS_PORT_ID_2, PORT_LABEL_2, GROUP_NAME_TWO);
    getNocDriver().linkPhysicalPort(NMS_PORT_ID_4, PORT_LABEL_4, GROUP_NAME_FOUR);
  }

  @After
  public void unlinkPort() {
    getUserDriver().switchToManager(GROUP_NAME_ONE);
    getManagerDriver().deleteVirtualPort(VP_LABEL_1);

    getUserDriver().switchToManager(GROUP_NAME_TWO);
    getManagerDriver().deleteVirtualPort(VP_LABEL_2);

    getManagerDriver().switchToNoc();
    getNocDriver().unlinkPhysicalPort(BOD_PORT_ID_1);
    getNocDriver().unlinkPhysicalPort(BOD_PORT_ID_2);
    getNocDriver().unlinkPhysicalPort(BOD_PORT_ID_4);
  }

  @Test
  public void shouldShowLogEventForOwnPhysicalPortAlignment() {
    // Allocation of port should visible in log for noc...
    getNocDriver().verifyLogEventExists(PORT_LABEL_1);
    getNocDriver().verifyLogEventExists(PORT_LABEL_2);
    getNocDriver().verifyLogEventExists(PORT_LABEL_4);

    getManagerDriver().switchToManager(GROUP_NAME_ONE);
    getManagerDriver().verifyLogEventExists(PORT_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_2);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_4);

    getManagerDriver().switchToManager(GROUP_NAME_TWO);
    getManagerDriver().verifyLogEventExists(PORT_LABEL_2);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_4);

    createVPOne();
    createVPTwo();

    // ... and for manager One ...
    getManagerDriver().switchToManager(GROUP_NAME_ONE);
    getManagerDriver().verifyLogEventExists(PORT_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_2);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_4);

    // ... and for manager One ...
    getManagerDriver().switchToManager(GROUP_NAME_TWO);
    getManagerDriver().verifyLogEventExists(PORT_LABEL_2);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_4);

    // ... and for user manager 2 ...
    getManagerDriver().switchToManager(GROUP_NAME_FOUR);
    getManagerDriver().verifyLogEventExists(PORT_LABEL_4);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_2);

    // ... and does exist for user
    getManagerDriver().switchToUser();
    getUserDriver().verifyLogEventExists(VP_LABEL_1);
    getUserDriver().verifyLogEventExists(VP_LABEL_2);
    getUserDriver().verifyLogEventDoesNotExist(VP_LABEL_4);
    getUserDriver().verifyLogEventDoesNotExist(PORT_LABEL_1);
    getUserDriver().verifyLogEventDoesNotExist(PORT_LABEL_2);
    getUserDriver().verifyLogEventDoesNotExist(PORT_LABEL_4);
  }

  @Test
  public void shouldShowVirtualPortAndReservationLogEvents() {
    getNocDriver().verifyLogEventDoesNotExist(VP_LABEL_1);
    getNocDriver().verifyLogEventDoesNotExist(VP_LABEL_2);
    getNocDriver().verifyLogEventExists(PORT_LABEL_1);
    getNocDriver().verifyLogEventExists(PORT_LABEL_2);
    getNocDriver().verifyLogEventExists(PORT_LABEL_4);

    // Not visible for managers
    getNocDriver().switchToManager(GROUP_NAME_ONE);
    getManagerDriver().verifyLogEventDoesNotExist(VP_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(VP_LABEL_2);
    getManagerDriver().verifyLogEventExists(PORT_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_2);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_4);

    getManagerDriver().switchToManager(GROUP_NAME_TWO);
    getManagerDriver().verifyLogEventDoesNotExist(VP_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(VP_LABEL_2);
    getManagerDriver().verifyLogEventExists(PORT_LABEL_2);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_4);

    getManagerDriver().switchToManager(GROUP_NAME_FOUR);
    getManagerDriver().verifyLogEventDoesNotExist(VP_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(VP_LABEL_2);
    getManagerDriver().verifyLogEventExists(PORT_LABEL_4);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_2);

    // Neither for users
    getManagerDriver().switchToUser();
    getUserDriver().verifyLogEventDoesNotExist(VP_LABEL_1);
    getUserDriver().verifyLogEventDoesNotExist(VP_LABEL_2);
    getUserDriver().verifyLogEventDoesNotExist(PORT_LABEL_1);
    getUserDriver().verifyLogEventDoesNotExist(PORT_LABEL_2);
    getUserDriver().verifyLogEventDoesNotExist(PORT_LABEL_4);

    // Create reservation, which links the VP in a VRG and therefore from now on
    // Both managers should see the related events
    createVPOne();

    getUserDriver().switchToNoc();
    getNocDriver().verifyLogEventExists(VP_LABEL_1);
    getNocDriver().verifyLogEventDoesNotExist(VP_LABEL_2);
    getNocDriver().verifyLogEventExists(PORT_LABEL_1);
    getNocDriver().verifyLogEventExists(PORT_LABEL_2);
    getNocDriver().verifyLogEventExists(PORT_LABEL_4);

    getManagerDriver().switchToManager(GROUP_NAME_ONE);
    getManagerDriver().verifyLogEventExists(VP_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(VP_LABEL_2);
    getManagerDriver().verifyLogEventExists(PORT_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_2);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_4);

    // Not in same VRG yet...
    getManagerDriver().switchToManager(GROUP_NAME_TWO);
    getManagerDriver().verifyLogEventDoesNotExist(VP_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(VP_LABEL_2);
    getManagerDriver().verifyLogEventExists(PORT_LABEL_2);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_4);

    getManagerDriver().switchToManager(GROUP_NAME_FOUR);
    getManagerDriver().verifyLogEventDoesNotExist(VP_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(VP_LABEL_2);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_2);
    getManagerDriver().verifyLogEventExists(PORT_LABEL_4);

    getManagerDriver().switchToUser();
    getUserDriver().verifyLogEventExists(VP_LABEL_1);
    getUserDriver().verifyLogEventDoesNotExist(VP_LABEL_2);
    getUserDriver().verifyLogEventDoesNotExist(PORT_LABEL_1);
    getUserDriver().verifyLogEventDoesNotExist(PORT_LABEL_2);
    getUserDriver().verifyLogEventDoesNotExist(PORT_LABEL_4);

    // Put manager two in same vrg, now VP events should be visible for each
    // other, PP not
    createVPTwo();

    getUserDriver().switchToNoc();
    getNocDriver().verifyLogEventExists(VP_LABEL_1);
    getNocDriver().verifyLogEventExists(VP_LABEL_2);
    getNocDriver().verifyLogEventExists(PORT_LABEL_1);
    getNocDriver().verifyLogEventExists(PORT_LABEL_2);
    getNocDriver().verifyLogEventExists(PORT_LABEL_4);

    getManagerDriver().switchToManager(GROUP_NAME_ONE);
    getManagerDriver().verifyLogEventExists(VP_LABEL_1);
    getManagerDriver().verifyLogEventExists(VP_LABEL_2);
    getManagerDriver().verifyLogEventExists(PORT_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_2);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_4);

    getManagerDriver().switchToManager(GROUP_NAME_TWO);
    getManagerDriver().verifyLogEventExists(VP_LABEL_1);
    getManagerDriver().verifyLogEventExists(VP_LABEL_2);
    getManagerDriver().verifyLogEventExists(PORT_LABEL_2);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_4);

    // Manager four is not in VRG
    getManagerDriver().switchToManager(GROUP_NAME_FOUR);
    getManagerDriver().verifyLogEventDoesNotExist(VP_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(VP_LABEL_2);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_2);
    getManagerDriver().verifyLogEventExists(PORT_LABEL_4);

    getManagerDriver().switchToUser();
    getUserDriver().verifyLogEventExists(VP_LABEL_1);
    getUserDriver().verifyLogEventExists(VP_LABEL_2);
    getUserDriver().verifyLogEventDoesNotExist(PORT_LABEL_1);
    getUserDriver().verifyLogEventDoesNotExist(PORT_LABEL_2);
    getUserDriver().verifyLogEventDoesNotExist(PORT_LABEL_4);

  }

  private void createVPOne() {
    // Now create Virtual Ports, and thereby group PhysicalPorts in same group
    getUserDriver().requestVirtualPort("selenium-users");
    getUserDriver().selectInstituteAndRequest(GROUP_NAME_ONE, 1200, "port 1");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().createVirtualPort(VP_LABEL_1);
  }

  private void createVPTwo() {
    getUserDriver().requestVirtualPort("selenium-users");
    getUserDriver().selectInstituteAndRequest(GROUP_NAME_TWO, 1200, "port 2");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().createVirtualPort(VP_LABEL_2);
  }

}
