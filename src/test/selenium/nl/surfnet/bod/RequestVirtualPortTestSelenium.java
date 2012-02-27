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

import java.util.concurrent.TimeUnit;

import nl.surfnet.bod.support.TestExternalSupport;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.util.concurrent.Uninterruptibles;

public class RequestVirtualPortTestSelenium extends TestExternalSupport {

  @Before
  public void setup() {
    getNocDriver().createNewPhysicalResourceGroup("SURFnet bv", ICT_MANAGERS_GROUP, "test@test.nl");
    getWebDriver().clickLinkInLastEmail();
    getNocDriver().linkPhysicalPort("00-21-E1-D6-D6-70_ETH10G-1-13-1", "Request a virtual port", "SURFnet bv");
  }

  @Test
  public void requestAVirtualPort() {
    getWebDriver().selectInstituteAndRequest("SURFnet bv", "I would like to have a new port");

    getWebDriver().clickLinkInLastEmail();

    Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);

    getManagerDriver().verifyNewVirtualPortHasPhysicalResourceGroup("SURFnet bv");
  }

  @After
  public void teardown() {
    getNocDriver().unlinkPhysicalPort("00-21-E1-D6-D6-70_ETH10G-1-13-1");
    getNocDriver().deletePhysicalGroup("SURFnet bv");
  }

}