package nl.surfnet.bod;

import nl.surfnet.bod.support.TestExternalSupport;

import org.junit.Test;

public class VirtualPortTestSelenium extends TestExternalSupport {

  @Test
  public void createAVirtualPort() {
    getWebDriver().createNewVirtualPort("My first virtual port");

    getWebDriver().verifyVirtualPortWasCreated("My first virtual port");
  }
}
