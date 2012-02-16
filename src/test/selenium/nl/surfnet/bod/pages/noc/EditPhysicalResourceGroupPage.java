package nl.surfnet.bod.pages.noc;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class EditPhysicalResourceGroupPage extends NewPhysicalResourceGroupPage {

  private EditPhysicalResourceGroupPage(WebDriver driver) {
    super(driver);
  }

  public static EditPhysicalResourceGroupPage get(RemoteWebDriver driver) {
    EditPhysicalResourceGroupPage page = new EditPhysicalResourceGroupPage(driver);
    PageFactory.initElements(driver, page);
    return page;
  }

}
