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

public class VirtualResourceGroupTestSelenium extends TestExternalSupport {

  public void createAndDeleteVirtualResourceGroup() throws Exception {
    getWebDriver().createNewVirtualResourceGroup("My First Virtual Group");

    getWebDriver().verifyVirtualResourceGroupWasCreated("My First Virtual Group");

    getWebDriver().createNewVirtualResourceGroup("My First Virtual Group");

    getWebDriver().verifyHasValidationError();

    getWebDriver().deleteVirtualResourceGroup("My First Virtual Group");

    getWebDriver().verifyVirtualResourceGroupWasDeleted("My First Virtual Group");

  }

}
