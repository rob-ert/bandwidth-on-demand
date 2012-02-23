package nl.surfnet.bod.pages.noc;

import nl.surfnet.bod.pages.AbstractListPage;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class ListUnallocatedPortsPage extends AbstractListPage {

  private static final String PAGE = "noc/physicalports/free";

  public ListUnallocatedPortsPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static ListUnallocatedPortsPage get(RemoteWebDriver driver, String urlUnderTest) {
    driver.get(urlUnderTest + PAGE);
    return get(driver);
  }

  public static ListUnallocatedPortsPage get(RemoteWebDriver driver) {
    ListUnallocatedPortsPage page = new ListUnallocatedPortsPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public EditPhysicalPortPage edit(String networkElementPk) {
    editRow(networkElementPk);

    return EditPhysicalPortPage.get(driver);
  }

}
