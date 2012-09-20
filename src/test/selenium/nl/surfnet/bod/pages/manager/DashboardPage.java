package nl.surfnet.bod.pages.manager;

import nl.surfnet.bod.pages.AbstractListPage;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class DashboardPage extends AbstractListPage {

  private static final String PAGE = "/manager";

  public DashboardPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static DashboardPage get(RemoteWebDriver driver) {
    DashboardPage page = new DashboardPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public static DashboardPage get(RemoteWebDriver driver, String urlUnderTest) {
    driver.get(urlUnderTest + PAGE);
    return get(driver);
  }

}
