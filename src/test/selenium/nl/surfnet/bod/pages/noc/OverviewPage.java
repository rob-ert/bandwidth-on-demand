package nl.surfnet.bod.pages.noc;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

import nl.surfnet.bod.pages.AbstractPage;

public class OverviewPage extends AbstractPage {

  private OverviewPage(RemoteWebDriver driver) {
    super(driver);
  }
  
  public static OverviewPage get(RemoteWebDriver driver, String urlUnderTest) {
    driver.get(urlUnderTest + "/noc");
    OverviewPage page = new OverviewPage(driver);
    PageFactory.initElements(driver, page);
    
    return page;
  }

}
