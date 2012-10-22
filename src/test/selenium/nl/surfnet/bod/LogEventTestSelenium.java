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
    getManagerDriver().switchToNoc();
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
    // Noc
    getUserDriver().switchToNoc();
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
