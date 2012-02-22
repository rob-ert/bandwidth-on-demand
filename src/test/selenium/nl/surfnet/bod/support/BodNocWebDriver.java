package nl.surfnet.bod.support;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import nl.surfnet.bod.pages.noc.EditPhysicalPortPage;
import nl.surfnet.bod.pages.noc.ListAllocatedPortsPage;
import nl.surfnet.bod.pages.noc.ListUnallocatedPortsPage;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.remote.RemoteWebDriver;

public class BodNocWebDriver {

  private final RemoteWebDriver driver;

  public BodNocWebDriver(RemoteWebDriver driver) {
    this.driver = driver;
  }

  public void linkPhysicalPort(String networkElementPk, String nocLabel) {
    ListUnallocatedPortsPage listPage = ListUnallocatedPortsPage.get(driver, BodWebDriver.URL_UNDER_TEST);

    EditPhysicalPortPage editPage = listPage.editRow(networkElementPk);
    editPage.sendNocLabel(nocLabel);
    editPage.save();
  }

  public void verifyPhysicalPortWasAllocated(String networkElementPk, String nocLabel) {
    ListAllocatedPortsPage page = ListAllocatedPortsPage.get(driver, BodWebDriver.URL_UNDER_TEST);

    page.findRow(networkElementPk, nocLabel);
  }

  public void unlinkPhysicalPort(String networkElementPk) {
    ListAllocatedPortsPage page = ListAllocatedPortsPage.get(driver, BodWebDriver.URL_UNDER_TEST);

    page.unlinkPhysicalPort(networkElementPk);
  }

  public void verifyPhysicalPortWasUnlinked(String networkElementPk) {
    ListAllocatedPortsPage page = ListAllocatedPortsPage.get(driver);

    assertTrue(page.containsAnyItems());

    try {
      page.findRow(networkElementPk);
      fail("The physical port was not unlinked");
    }
    catch (NoSuchElementException e) {
      // expected
    }
  }
}
