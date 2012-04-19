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

public class InactivePhysicalResourceGroupManagerTestSelenium extends TestExternalSupport {

  private static final String WRONG_EMAIL = "wrong@example.com";

  @Before
  public void setup() {
    getNocDriver().createNewPhysicalResourceGroup("SURFnet bv", ICT_MANAGERS_GROUP, WRONG_EMAIL);
  }

  @Test
  public void anInactivePhysicalResourceGroupShouldGiveARedirectForManager() throws Exception {
    getManagerDriver().switchRoleManager();

    getManagerDriver().verifyOnEditPhysicalResourceGroupPage(WRONG_EMAIL);
  }

   @After
  public void teardown() {
    getNocDriver().deletePhysicalResourceGroup("SURFnet bv");
  }
}
