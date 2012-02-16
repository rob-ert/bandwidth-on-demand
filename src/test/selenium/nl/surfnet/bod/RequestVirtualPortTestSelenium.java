package nl.surfnet.bod;

import nl.surfnet.bod.support.TestExternalSupport;

import org.junit.Test;

public class RequestVirtualPortTestSelenium extends TestExternalSupport {

  @Test
  public void requestAVirtualPort() {
    getWebDriver().selectInstituteAndRequest("Universiteit Utrecht (UU)", "I would like to have a new port");

    getWebDriver().clickLinkInLastEmail();

    getWebDriver().verifyNewVirtualPortHasPhysicalResourceGroup("Universiteit Utrecht");
  }

}