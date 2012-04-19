package nl.surfnet.bod.pages.manager;

import nl.surfnet.bod.pages.AbstractPage;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class ManagerOverviewPage extends AbstractPage {

  private ManagerOverviewPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static ManagerOverviewPage get(RemoteWebDriver driver, String urlUnderTest) {
    driver.get(urlUnderTest + "/manager");
    ManagerOverviewPage page = new ManagerOverviewPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

}
