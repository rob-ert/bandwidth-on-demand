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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import nl.surfnet.bod.support.TestExternalSupport;

public class LogEventTestSelenium extends TestExternalSupport {

  private static final String GROUP_NAME_ONE = "2COLLEGE";
  private static final String GROUP_NAME_TWO = "SURFnet bv";
  private static final String PORT_LABEL_1 = "NOC Port 1";
  private static final String PORT_LABEL_2 = "NOC Port 2";
  private static final String VP_LABEL_1 = "VP Port 1";

  @Before
  public void prepareUser() {
    getNocDriver().createNewPhysicalResourceGroup(GROUP_NAME_ONE, ICT_MANAGERS_GROUP, "test@test.nl");
    getWebDriver().clickLinkInLastEmail();
    getNocDriver().createNewPhysicalResourceGroup(GROUP_NAME_TWO, ICT_MANAGERS_GROUP_2, "test2@test.nl");
    getWebDriver().clickLinkInLastEmail();

    getNocDriver().linkPhysicalPort(NMS_PORT_ID_1, PORT_LABEL_1, GROUP_NAME_ONE);
    getNocDriver().linkPhysicalPort(NMS_PORT_ID_2, PORT_LABEL_2, GROUP_NAME_TWO);

    getUserDriver().requestVirtualPort("selenium-users");
    getUserDriver().selectInstituteAndRequest(GROUP_NAME_ONE, 1200, "port 1");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().createVirtualPort(VP_LABEL_1);
  }

  @After
  public void unlinkPort() {
    getUserDriver().switchToManager(GROUP_NAME_ONE);
    getManagerDriver().deleteVirtualPort(VP_LABEL_1);

    getManagerDriver().switchToNoc();
    getNocDriver().unlinkPhysicalPort(BOD_PORT_ID_1);
    getNocDriver().unlinkPhysicalPort(BOD_PORT_ID_2);
  }

  @Test
  public void shouldShowLogEventForPhysicalPortAlignment() {
    getManagerDriver().switchToNoc();
    // Allocation of port should visible in log for noc...
    getNocDriver().verifyLogEventExistis(PORT_LABEL_1);

    // ... and for manager One ...
    getManagerDriver().switchToManager(GROUP_NAME_ONE);
    getManagerDriver().verifyLogEventExists(PORT_LABEL_1);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_2);

    // ... and for manager One ...
    getManagerDriver().switchToManager(GROUP_NAME_TWO);
    getManagerDriver().verifyLogEventExists(PORT_LABEL_2);
    getManagerDriver().verifyLogEventDoesNotExist(PORT_LABEL_1);

    // ... and does exist for user
    getManagerDriver().switchToUser();
    getUserDriver().verifyLogEventExists(VP_LABEL_1);
    getUserDriver().verifyLogEventDoesNotExist(PORT_LABEL_1);
    getUserDriver().verifyLogEventDoesNotExist(PORT_LABEL_2);
  }
}
