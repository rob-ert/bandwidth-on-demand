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

public class VirtualPortTestSelenium extends TestExternalSupport {

  @Before
  public void setup() {
    getNocDriver().createNewPhysicalResourceGroup("Universiteit Utrecht", ICT_MANAGERS_GROUP, "test@example.com");
    getNocDriver().linkPhysicalPort(NETWORK_ELEMENT_PK, "First port", "Universiteit Utrecht");
    getNocDriver().linkPhysicalPort(NETWORK_ELEMENT_PK_2, "Second port", "Universiteit Utrecht");
    getWebDriver().refreshGroups();
    getManagerDriver().createNewVirtualResourceGroup("Selenium researchers", USERS_GROUP);
  }

  @Test
  public void createAVirtualPort() {
    getManagerDriver().createNewVirtualPort("My first virtual port", 100, null, null, "Second port");

    getManagerDriver().verifyVirtualPortExists("My first virtual port", "100", "Universiteit Utrecht",
        "Selenium researchers", "Second port");

    getManagerDriver().editVirtualPort("My first virtual port", "My edited virtual port", 900, "20");

    getManagerDriver().verifyVirtualPortExists("My edited virtual port", "900", "20");

    getWebDriver().editVirtualPort("My edited virtual port", "User label");

    getWebDriver().verifyVirtualPortExists("User label");

    getManagerDriver().deleteVirtualPort("My edited virtual port");

    getManagerDriver().verifyVirtualPortWasDeleted("My edited virtual port");
  }

  @After
  public void teardown() {
    getManagerDriver().deleteVirtualResourceGroup("Selenium researchers");
    getNocDriver().unlinkPhysicalPort(NETWORK_ELEMENT_PK);
    getNocDriver().deletePhysicalResourceGroup("Universiteit Utrecht");
  }

}
