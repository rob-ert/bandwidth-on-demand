package nl.surfnet.bod.pages.noc;

import nl.surfnet.bod.pages.AbstractListPage;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class ListAllocatedPortsPage extends AbstractListPage {

  private static final String PAGE = "noc/physicalports";

  public ListAllocatedPortsPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static ListAllocatedPortsPage get(RemoteWebDriver driver, String urlUnderTest) {
    driver.get(urlUnderTest + PAGE);
    return get(driver);
  }

  public static ListAllocatedPortsPage get(RemoteWebDriver driver) {
    ListAllocatedPortsPage page = new ListAllocatedPortsPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public void unlinkPhysicalPort(String networkElementPk) {
    delete(networkElementPk);
  }
}
