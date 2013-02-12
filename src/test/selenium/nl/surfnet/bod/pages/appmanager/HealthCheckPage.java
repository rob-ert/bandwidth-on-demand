package nl.surfnet.bod.pages.appmanager;

import nl.surfnet.bod.pages.AbstractPage;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class HealthCheckPage extends AbstractPage {

  private static final String PAGE = "/healthcheck";

  public HealthCheckPage(RemoteWebDriver driver) {
    super(driver);
  }

  public static HealthCheckPage get(RemoteWebDriver driver) {
    HealthCheckPage page = new HealthCheckPage(driver);
    PageFactory.initElements(driver, page);

    return page;
  }

  public static HealthCheckPage get(RemoteWebDriver driver, String urlUnderTest) {
    driver.get(urlUnderTest + PAGE);
    return get(driver);
  }

  public void verifyIsCurrentPage() {
    super.verifyIsCurrentPage(PAGE);
  }
}
