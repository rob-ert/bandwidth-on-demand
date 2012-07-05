package nl.surfnet.bod.pages.noc;

import nl.surfnet.bod.pages.AbstractListPage;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class ListVirtualResourceGroupPage extends AbstractListPage {

  private static final String PAGE =  "/noc/teams";

  public ListVirtualResourceGroupPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static ListVirtualResourceGroupPage get(RemoteWebDriver driver, String baseUrl) {
    driver.get(baseUrl + PAGE);
    return get(driver);
  }

  public static ListVirtualResourceGroupPage get(RemoteWebDriver driver) {
    ListVirtualResourceGroupPage page = new ListVirtualResourceGroupPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

}
