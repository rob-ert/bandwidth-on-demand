/**
 * Copyright (c) 2012, 2013 SURFnet BV
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *     disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the distribution.
 *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package nl.surfnet.bod;

import nl.surfnet.bod.support.TestExternalSupport;

import org.junit.Test;

public class PhysicalResourceGroupTestSelenium extends TestExternalSupport {

  @Test
  public void createActivateEditAndDeleteApiBasedPhysicalResourceGroup() throws Exception {
    String initialEmail = "truus@example.com";
    String finalEmail = "henk@example.com";

    getNocDriver().createNewApiBasedPhysicalResourceGroup(GROUP_RUG, ICT_MANAGERS_GROUP, initialEmail);

    getNocDriver().verifyGroupWasCreated(GROUP_RUG, initialEmail);

    getWebDriver().verifyLastEmailRecipient(initialEmail);

    getWebDriver().clickLinkInLastEmail();

    getNocDriver().verifyPhysicalResourceGroupIsActive(GROUP_RUG, initialEmail);

    getNocDriver().editPhysicalResourceGroup(GROUP_RUG, finalEmail);

    getNocDriver().verifyGroupExists(GROUP_RUG, finalEmail, false);

    getWebDriver().verifyLastEmailRecipient(finalEmail);

    getNocDriver().deletePhysicalResourceGroup(GROUP_RUG);
  }

  @Test
  public void createAndActiveSabBasedPhysicalResourceGroup() {
    String email = "managers@example.com";

    getNocDriver().createNewSabBasedPhysicalResourceGroup(GROUP_SURFNET, email);

    getNocDriver().verifyGroupWasCreated(GROUP_SURFNET, email);

    getWebDriver().clickLinkInLastEmail();

    getWebDriver().verifyPageHasModalHeader("Access Denied");

    getNocDriver().switchToUserHans();

    getNocDriver().switchToManagerRole(GROUP_SURFNET);

    getWebDriver().clickLinkInLastEmail();

    getWebDriver().verifyPageHasModalHeader("Email address is confirmed");

    getNocDriver().verifyPhysicalResourceGroupIsActive(GROUP_SURFNET, email);
  }

  @Test
  public void verifyNocLinkFromInstituteToPhysicalPorts() {
    getNocDriver().createNewApiBasedPhysicalResourceGroup(GROUP_SARA, ICT_MANAGERS_GROUP, "test@example.com");

    getNocDriver().addPhysicalPortToInstitute(GROUP_SARA, "NOC 1 label", "Mock_Poort 2de verdieping toren1b");
    getNocDriver().addPhysicalPortToInstitute(GROUP_SARA, "NOC 2 label", "Mock_Poort 4de verdieping toren1a");

    getNocDriver().verifyPhysicalResourceGroupToPhysicalPortsLink(GROUP_SARA);
  }

}