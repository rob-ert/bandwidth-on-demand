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

import org.junit.Test;

public class PhysicalResourceGroupTestSelenium extends TestExternalSupport {

  @Test
  public void createActivateEditAndDeletePhysicalResourceGroup() throws Exception {
    String institute = "SURFnet bv";
    String initialEmail = "truus@example.com";
    String finalEmail = "henk@example.com";

    getNocDriver().createNewPhysicalResourceGroup(institute, ICT_MANAGERS_GROUP, initialEmail);

    getNocDriver().verifyGroupWasCreated(institute, initialEmail);

    getWebDriver().verifyLastEmailRecipient(initialEmail);

    getWebDriver().clickLinkInLastEmail();

    getNocDriver().verifyPhysicalResourceGroupIsActive(institute, initialEmail);

    getNocDriver().editPhysicalResourceGroup(institute, finalEmail);

    getNocDriver().verifyGroupExists(institute, finalEmail, false);

    getWebDriver().verifyLastEmailRecipient(finalEmail);

    getNocDriver().deletePhysicalResourceGroup(institute);
  }

  @Test
  public void verifyNocLinkFromInstituteToPhysicalPorts() {
    final String instituteName = "ASTRON";
    getNocDriver().createNewPhysicalResourceGroup(instituteName, ICT_MANAGERS_GROUP, "test@example.com");

    getNocDriver().addPhysicalPortToInstitute(instituteName, "NOC 1 label", "Mock_Poort 1de verdieping toren1a");
    getNocDriver().addPhysicalPortToInstitute(instituteName, "NOC 2 label", "Mock_Poort 2de verdieping toren1b");

    getNocDriver().verifyPhysicalResourceGroupToPhysicalPortsLink(instituteName);
  }

}