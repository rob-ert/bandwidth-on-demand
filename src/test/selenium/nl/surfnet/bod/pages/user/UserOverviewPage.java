package nl.surfnet.bod.pages.user;

import nl.surfnet.bod.pages.AbstractPage;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class UserOverviewPage extends AbstractPage {

  private UserOverviewPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static UserOverviewPage get(RemoteWebDriver driver, String urlUnderTest) {
    driver.get(urlUnderTest + "/user");
    UserOverviewPage page = new UserOverviewPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

}
