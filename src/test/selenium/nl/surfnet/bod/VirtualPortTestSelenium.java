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

public class VirtualPortTestSelenium extends TestExternalSupport {

  private static final String VP_DELETE_ALERT_TEXT = "reservations will be effected";

  @Before
  public void setup() {
    getNocDriver().createNewPhysicalResourceGroup("2COLLEGE", ICT_MANAGERS_GROUP, "test@test.nl");
    getNocDriver().linkPhysicalPort(NMS_PORT_ID_2, "Request a virtual port", "2COLLEGE");

    getWebDriver().clickLinkInLastEmail();

    getUserDriver().requestVirtualPort("selenium-users");
    getUserDriver().selectInstituteAndRequest("2COLLEGE", 1000, "port 1");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().createVirtualPort("First port");

    getUserDriver().requestVirtualPort("selenium-users");
    getUserDriver().selectInstituteAndRequest("2COLLEGE", 1000, "port 2");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().createVirtualPort("Second port");
  }

  @Test
  public void lastPortDeletedShouldDeleteVirtualResourceGroup() {
    getManagerDriver().verifyVirtualResourceGroupExists("selenium-users", "2");

    getManagerDriver().deleteVirtualPortAndVerifyAlertText("First port", VP_DELETE_ALERT_TEXT);

    getManagerDriver().switchToUser();

    getUserDriver().verifyMemberOf("selenium-users");

    getUserDriver().switchToManager("2COLLEGE");
    getManagerDriver().verifyVirtualResourceGroupExists("selenium-users", "1");

    getManagerDriver().switchToNoc();
    getNocDriver().verifyVirtualResourceGroupExists("selenium-users", "1");

    getUserDriver().switchToManager("2COLLEGE");
    getManagerDriver().deleteVirtualPortAndVerifyAlertText("Second port", VP_DELETE_ALERT_TEXT);

    getManagerDriver().verifyVirtualResourceGroupsEmpty();

    getManagerDriver().switchToUser();

    getUserDriver().verifyNotMemberOf("selenium-users");
  }
}
