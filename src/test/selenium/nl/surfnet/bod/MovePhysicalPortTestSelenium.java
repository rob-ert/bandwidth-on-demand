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

import org.junit.Before;
import org.junit.Test;

import nl.surfnet.bod.support.TestExternalSupport;

public class MovePhysicalPortTestSelenium extends TestExternalSupport {

  private static final String INSTITUTE_SURF = "SURFnet Netwerk";
  private static final String INSTITUTE_UU = "Universiteit Utrecht";

  @Before
  public void setup() {
    getNocDriver().createNewPhysicalResourceGroup(INSTITUTE_SURF, ICT_MANAGERS_GROUP, "test@example.com");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().switchToNoc();

    getNocDriver().createNewPhysicalResourceGroup(INSTITUTE_UU, ICT_MANAGERS_GROUP_2, "test@example.com");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().switchToNoc();

    getNocDriver().linkPhysicalPort(NMS_PORT_ID_1, "First port", INSTITUTE_SURF);
    getNocDriver().linkPhysicalPort(NMS_PORT_ID_2, "Second port", INSTITUTE_UU);

    getNocDriver().switchToUser();
    getUserDriver().requestVirtualPort("selenium-users");
    getUserDriver().selectInstituteAndRequest(INSTITUTE_SURF, 1200, "port 1");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().createVirtualPort("First port");

    getManagerDriver().switchToUser();
    getUserDriver().requestVirtualPort("selenium-users");
    getUserDriver().selectInstituteAndRequest(INSTITUTE_UU, 1200, "port 2");
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().createVirtualPort("Second port");

    getManagerDriver().switchToUser();
    getUserDriver().createNewReservation("First reservation");
    getUserDriver().createNewReservation("Second reservation");
  }

  @Test
  public void moveAPhysicalPort() {
    getUserDriver().switchToNoc();

    getNocDriver().movePhysicalPort("First port");

    getNocDriver().verifyMovePage(NMS_PORT_ID_1, INSTITUTE_SURF, 1, 2, 2);

    getNocDriver().movePhysicalPortChooseNewPort(NMS_PORT_ID_3);

    getNocDriver().verifyMoveResultPage(2);

    getNocDriver().verifyHasReservations(4);
  }

}
