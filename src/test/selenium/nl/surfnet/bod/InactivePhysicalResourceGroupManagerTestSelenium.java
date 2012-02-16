package nl.surfnet.bod;

import nl.surfnet.bod.support.TestExternalSupport;

import org.junit.Test;

public class InactivePhysicalResourceGroupManagerTestSelenium extends TestExternalSupport {

  @Test
  public void anInactivePhysicalResourceGroupShouldGiveARedirectForManager() throws Exception {
    getWebDriver().createNewPhysicalResourceGroup("SURFnet bv",
        "urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:ict-managers", "wrong@example.com");

    getWebDriver().managerDashboard();

    getWebDriver().verifyOnEditPhysicalResourceGroupPage("wrong@example.com");

    getWebDriver().deletePhysicalGroup("SURFnet bv");
  }
}
