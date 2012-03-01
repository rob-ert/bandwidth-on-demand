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

public class RequestVirtualPortTestSelenium extends TestExternalSupport {

  @Before
  public void setup() {
    getNocDriver().createNewPhysicalResourceGroup("2COLLEGE", ICT_MANAGERS_GROUP, "test@test.nl");
    getNocDriver().createNewPhysicalResourceGroup("SURFnet bv", ICT_MANAGERS_GROUP, "test@test.nl");
    getNocDriver().linkPhysicalPort(NETWORK_ELEMENT_PK, "Request a virtual port", "SURFnet bv");
    getNocDriver().linkPhysicalPort(NETWORK_ELEMENT_PK_2, "Request a virtual port", "2COLLEGE");

    getWebDriver().refreshGroups();
    getWebDriver().clickLinkInLastEmail();
    getManagerDriver().createNewVirtualResourceGroup("Selenium Onderzoekers", USERS_GROUP);
  }

  @Test
  public void requestAVirtualPort() {
    getWebDriver().verifyRequestVirtualPortInstituteInactive("2COLLEGE");

    getWebDriver().selectInstituteAndRequest("SURFnet bv", 1200, "I would like to have a new port");

    getWebDriver().clickLinkInLastEmail();

    getManagerDriver().verifyNewVirtualPortHasProperties("SURFnet bv", 1200);
  }

  @After
  public void teardown() {
    getManagerDriver().deleteVirtualResourceGroup("Selenium Onderzoekers");
    getNocDriver().unlinkPhysicalPort(NETWORK_ELEMENT_PK);
    getNocDriver().unlinkPhysicalPort(NETWORK_ELEMENT_PK_2);
    getNocDriver().deletePhysicalResourceGroup("SURFnet bv");
    getNocDriver().deletePhysicalResourceGroup("2COLLEGE");
  }

}