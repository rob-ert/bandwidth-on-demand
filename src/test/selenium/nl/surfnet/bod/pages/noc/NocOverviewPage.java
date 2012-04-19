package nl.surfnet.bod.pages.noc;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

import nl.surfnet.bod.pages.AbstractPage;

public class NocOverviewPage extends AbstractPage {

  private NocOverviewPage(RemoteWebDriver driver) {
    super(driver);
  }
  
  public static NocOverviewPage get(RemoteWebDriver driver, String urlUnderTest) {
    driver.get(urlUnderTest + "/noc");
    NocOverviewPage page = new NocOverviewPage(driver);
    PageFactory.initElements(driver, page);
    
    return page;
  }

}
