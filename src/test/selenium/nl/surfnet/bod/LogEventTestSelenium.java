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

import nl.surfnet.bod.support.TestExternalSupport;

import org.junit.Before;
import org.junit.Test;

public class LogEventTestSelenium extends TestExternalSupport {

  private static final String CREATE_ACTION = "Create";

  protected static final String ICT_USER_GROUP_2 =
    "urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:selenium-users2";

  private static final String PORT_LABEL_1 = "NOC Port 1";
  private static final String PORT_LABEL_2 = "NOC Port 2";
  private static final String PORT_LABEL_3 = "NOC Port 3";
  private static final String VP_LABEL_1 = "VP Port 1";
  private static final String VP_LABEL_2 = "VP Port 2";

  @Before
  public void prepareUser() {
    getNocDriver().createNewApiBasedPhysicalResourceGroup(GROUP_SARA, ICT_MANAGERS_GROUP, "test@test.nl");
    getWebDriver().clickLinkInLastEmail();

    getNocDriver().createNewApiBasedPhysicalResourceGroup(GROUP_SURFNET, ICT_MANAGERS_GROUP_2, "test2@test.nl");
    getWebDriver().clickLinkInLastEmail();

    getNocDriver().createNewApiBasedPhysicalResourceGroup(GROUP_RUG, ICT_USER_GROUP_2, "test3@test.nl");
    getWebDriver().clickLinkInLastEmail();

    getNocDriver().linkUniPort(NMS_NOVLAN_PORT_ID_1, PORT_LABEL_1, GROUP_SARA);
    getNocDriver().linkUniPort(NMS_PORT_ID_2, PORT_LABEL_2, GROUP_SURFNET);
    getNocDriver().linkUniPort(NMS_PORT_ID_3, PORT_LABEL_3, GROUP_RUG);
  }

  @Test
  public void shouldOnlyShowLogEventsForSelectedRole() {
    getNocDriver().verifyLogEventExists(CREATE_ACTION, PORT_LABEL_1);
    getNocDriver().verifyLogEventExists(CREATE_ACTION, PORT_LABEL_2);
    getNocDriver().verifyLogEventExists(CREATE_ACTION, PORT_LABEL_3);

    getManagerDriver().switchToManagerRole(GROUP_SARA);
    getManagerDriver().verifyLogEventExists(CREATE_ACTION, PORT_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_2);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_3);

    getManagerDriver().switchToManagerRole(GROUP_SURFNET);
    getManagerDriver().verifyLogEventExists(CREATE_ACTION, PORT_LABEL_2);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_3);

    getManagerDriver().switchToManagerRole(GROUP_RUG);
    getManagerDriver().verifyLogEventExists(CREATE_ACTION, PORT_LABEL_3);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_2);

    getManagerDriver().switchToUserRole();
    getUserDriver().verifyLogEventDoesNotExist(PORT_LABEL_1);
    getUserDriver().verifyLogEventDoesNotExist(PORT_LABEL_2);
    getUserDriver().verifyLogEventDoesNotExist(PORT_LABEL_3);

    createVPOneForGroupOne();
    createVPTwoForGroupTwo();

    // NOC sees all
    getUserDriver().switchToNocRole();
    getNocDriver().verifyLogEventExists(CREATE_ACTION, VP_LABEL_1);
    getNocDriver().verifyLogEventExists(CREATE_ACTION, VP_LABEL_2);

    // Manager 1 only sees his pp and vp
    getManagerDriver().switchToManagerRole(GROUP_SARA);
    getManagerDriver().verifyLogEventExists(CREATE_ACTION, VP_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(VP_LABEL_2);

    // Manager 2 only sees his pp and vp
    getManagerDriver().switchToManagerRole(GROUP_SURFNET);
    getManagerDriver().verifyLogEventExists(CREATE_ACTION, VP_LABEL_2);
    getManagerDriver().verifyLogEventDoesNotExist(VP_LABEL_1);

    // Manager 3 only sees his pp
    getManagerDriver().switchToManagerRole(GROUP_RUG);
    getManagerDriver().verifyLogEventDoesNotExist(VP_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(VP_LABEL_2);

    // User sees only the vp
    getManagerDriver().switchToUserRole();
    getUserDriver().verifyLogEventExists(CREATE_ACTION, VP_LABEL_1);
    getUserDriver().verifyLogEventExists(CREATE_ACTION, VP_LABEL_2);

    getUserDriver().createNewReservation("Testing log events");
    getUserDriver().verifyLogEventExists(CREATE_ACTION, "Testing log events");

    getUserDriver().switchToManagerRole(GROUP_SARA);
    getWebDriver().takeScreenshot("after_switch_to_sara");
    getManagerDriver().verifyLogEventExists(CREATE_ACTION, "Testing log events");

    getManagerDriver().switchToManagerRole(GROUP_SURFNET);
    getManagerDriver().verifyLogEventExists(CREATE_ACTION, "Testing log events");

    getManagerDriver().switchToManagerRole(GROUP_RUG);
    getManagerDriver().verifyLogEventDoesNotExist("Testing log events");
  }

  private void createVPOneForGroupOne() {
    // Now create Virtual Ports, and thereby group PhysicalPorts in same group
    getUserDriver().requestVirtualPort("Selenium users");
    getUserDriver().selectInstituteAndRequest(GROUP_SARA, 1200, "port 1");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().acceptVirtualPort(PORT_LABEL_1, VP_LABEL_1, Optional.<String>absent(), Optional.<Integer>absent());
  }

  private void createVPTwoForGroupTwo() {
    getUserDriver().requestVirtualPort("Selenium users");
    getUserDriver().selectInstituteAndRequest(GROUP_SURFNET, 1200, "port 2");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().acceptVirtualPort(PORT_LABEL_2, VP_LABEL_2, Optional.<String>absent(), Optional.of(23));
  }

}